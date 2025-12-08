// src/main/java/com/popspot/popupplatform/service/user/UserReportService.java

package com.popspot.popupplatform.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.UserLimitInfoDto;
import com.popspot.popupplatform.dto.user.report.*;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserReportService {

    private final ChatClient chatClient;
    private final UserMapper userMapper;
    private final PopupMapper popupMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public UserReportService(ChatClient.Builder builder,
                             UserMapper userMapper,
                             PopupMapper popupMapper) {

        this.userMapper = userMapper;
        this.popupMapper = popupMapper;

        this.chatClient = builder
                .defaultSystem("""
                        너는 팝업 스토어 플랫폼의 데이터 분석가이자 카피라이터다.
                        - 입력으로 유저의 행동 데이터/통계가 JSON 으로 들어온다.
                        - 너의 역할은 이 데이터를 기반으로 '개인화된 성향 리포트'를 만들어 주는 것이다.
                        - 차트용 수치는 그대로 유지하면서, 설명 텍스트를 사람 친화적으로, 너무 과장되지 않게 작성한다.
                        - 모든 응답은 한국어로 작성한다.
                        """)
                .build();
    }

    /**
     * 유저 리포트 메인 엔트리
     */
    public UserPersonaReport userReport(Long userId) {

        // 1. 유저 기본 인구통계 정보 (성별 / 출생년도)
        UserLimitInfoDto limitInfo = userMapper.findUserLimitInfo(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 또는 비활성화된 유저입니다. userId=" + userId));

        String gender = limitInfo.getUserGender();         // "M" / "F" / null 등
        Integer birthYear = limitInfo.getUserBirthyear();  // 1998 등

        // 나이/연령대 계산
        AgeInfo ageInfo = calculateAgeInfo(birthYear);

        // 2. 유저의 팝업 이용 히스토리 조회 (조회 / 찜 / 예약)
        // Mapper는 DTO(UserPopupEventDto)를 돌려주고, Service에서 내부 이벤트로 변환
        List<UserPopupEventDto> eventDtos = popupMapper.selectUserPopupEvents(userId);
        if (eventDtos == null) {
            eventDtos = Collections.emptyList();
        }

        // 3. 행동 스냅샷/피처 계산 (뷰/찜/예약 수, 해시태그/지역, 재방문율, 가격 성향 등)
        UserBehaviorSnapshot snapshot = buildBehaviorSnapshot(userId, gender, ageInfo, eventDtos);

        // 4. 육각형 축(점수) 1차 계산 (데이터 기반 점수)
        List<UserPersonaAxis> axesDraft = buildHexagonAxesFromSnapshot(snapshot);

        // 5. LLM 호출해서 축 설명/요약을 더 사람스럽게 다듬기
        PersonaLlmResult llmResult = callLlmForPersona(snapshot, axesDraft);

        List<UserPersonaAxis> finalAxes =
                llmResult != null && llmResult.axes() != null && !llmResult.axes().isEmpty()
                        ? llmResult.axes()
                        : axesDraft; // LLM 실패시 초안 사용

        String finalSummary =
                llmResult != null && llmResult.summary() != null
                        ? llmResult.summary()
                        : buildFallbackSummary(snapshot);

        // 6. 추천 팝업들 조회

        // 6-1) 나와 비슷한 성향(해시태그 기준) 유저들이 많이 보는 팝업
        List<UserPersonaPopupCard> similarTastePopups =
                recommendBySimilarTaste(userId, snapshot, 4);

        // 6-2) 같은 성별 + 연령대가 많이 보는 팝업
        List<UserPersonaPopupCard> demographicPopups =
                recommendByDemographic(gender, birthYear, 4);

        // 7. 해시태그/지역 TOP (유저 기준)
        List<UserPersonaTagStat> topHashtags = snapshot.getTopHashtags(8);
        List<UserPersonaRegionStat> topRegions = snapshot.getTopRegions(8);

        // 8. 최종 리포트 DTO 조립
        return UserPersonaReport.builder()
                .userId(userId)
                .gender(gender)
                .birthYear(birthYear)
                .age(ageInfo.age())
                // 예: "20대 초반", "30대 중반"
                .ageGroupLabel(ageInfo.detailLabel())
                // 예: "2025-09-01 ~ 2025-12-01 이용 기준"
                .periodLabel(snapshot.getAnalysisPeriodLabel())
                .totalViewCount(snapshot.getTotalViewCount())
                .totalWishlistCount(snapshot.getTotalWishlistCount())
                .totalReservationCount(snapshot.getTotalReservationCount())
                .axes(finalAxes)
                .summary(finalSummary)
                .similarTastePopups(similarTastePopups)
                .demographicPopups(demographicPopups)
                .topHashtags(topHashtags)
                .topRegions(topRegions)
                .build();
    }

    // -------------------------------------------------------
    // 1) 기본 나이/연령대 계산
    // -------------------------------------------------------

    private AgeInfo calculateAgeInfo(Integer birthYear) {
        if (birthYear == null || birthYear <= 0) {
            return new AgeInfo(null, null, null, "연령 정보 없음");
        }

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear + 1; // 한국식 나이 기준이면 +1, 아니면 빼도 됨

        int group = (age / 10) * 10; // 23 -> 20, 31 -> 30
        String groupLabel = group + "대";

        String detailLabel;
        int mod = age % 10;
        if (mod <= 3) {
            detailLabel = groupLabel + " 초반";
        } else if (mod <= 6) {
            detailLabel = groupLabel + " 중반";
        } else {
            detailLabel = groupLabel + " 후반";
        }

        return new AgeInfo(age, group, detailLabel, groupLabel);
    }

    // -------------------------------------------------------
    // 2) 히스토리 DTO -> 내부 이벤트 -> 행동 스냅샷 생성
    // -------------------------------------------------------

    private UserBehaviorSnapshot buildBehaviorSnapshot(Long userId,
                                                       String gender,
                                                       AgeInfo ageInfo,
                                                       List<UserPopupEventDto> eventDtos) {

        // DTO → 내부 이벤트로 변환 (타입/해시태그 파싱 포함)
        List<UserPopupEvent> events = eventDtos.stream()
                .map(dto -> {
                    UserPopupEventType type = UserPopupEventType.valueOf(dto.getEventType());
                    LocalDateTime ts = dto.getEventAt();
                    String priceType = dto.getPriceType();
                    Integer peopleCount = dto.getPeopleCount();
                    String region = dto.getRegion();

                    List<String> tags = Collections.emptyList();
                    if (dto.getHashtags() != null && !dto.getHashtags().isBlank()) {
                        tags = Arrays.stream(dto.getHashtags().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                    }

                    return new UserPopupEvent(
                            dto.getPopId(),
                            type,
                            ts,
                            priceType,
                            peopleCount,
                            region,
                            tags
                    );
                })
                .toList();

        if (events.isEmpty()) {
            return UserBehaviorSnapshot.empty(userId, gender, ageInfo);
        }

        int viewCount = 0;
        int wishlistCount = 0;
        int reservationCount = 0;

        Map<Long, Integer> popupViewCount = new HashMap<>();
        Map<Long, Integer> popupAnyActionCount = new HashMap<>();

        Map<String, Double> hashtagScore = new HashMap<>();
        Map<String, Double> regionScore = new HashMap<>();

        int freeCount = 0;
        int paidCount = 0;

        int groupVisitTotalPeople = 0;
        int groupVisitEvents = 0;

        int nightActions = 0;   // 20시 이후
        int weekendActions = 0; // 토/일

        LocalDateTime minEventTime = null;
        LocalDateTime maxEventTime = null;

        for (UserPopupEvent e : events) {

            // 전체 기간 범위 계산
            if (minEventTime == null || e.timestamp().isBefore(minEventTime)) {
                minEventTime = e.timestamp();
            }
            if (maxEventTime == null || e.timestamp().isAfter(maxEventTime)) {
                maxEventTime = e.timestamp();
            }

            // 액션별 카운트
            switch (e.type()) {
                case VIEW -> viewCount++;
                case WISHLIST -> wishlistCount++;
                case RESERVATION -> reservationCount++;
            }

            // 팝업별 액션/뷰
            popupAnyActionCount.merge(e.popId(), 1, Integer::sum);
            if (e.type() == UserPopupEventType.VIEW) {
                popupViewCount.merge(e.popId(), 1, Integer::sum);
            }

            // 가격 성향
            if ("FREE".equalsIgnoreCase(e.priceType())) {
                freeCount++;
            } else if ("PAID".equalsIgnoreCase(e.priceType())) {
                paidCount++;
            }

            // 동행 인원
            if (e.type() == UserPopupEventType.RESERVATION && e.peopleCount() != null) {
                groupVisitTotalPeople += e.peopleCount();
                groupVisitEvents++;
            }

            // 야행성/주말 방문 여부
            int hour = e.timestamp().getHour();
            if (hour >= 20 || hour < 6) {
                nightActions++;
            }
            DayOfWeek dow = e.timestamp().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendActions++;
            }

            // 해시태그/지역 점수 (VIEW=1, WISHLIST=2, RESERVATION=3 가중치)
            double weight = switch (e.type()) {
                case VIEW -> 1.0;
                case WISHLIST -> 2.0;
                case RESERVATION -> 3.0;
            };

            // 해시태그
            if (e.hashtags() != null) {
                for (String tag : e.hashtags()) {
                    if (tag == null || tag.isBlank()) continue;
                    hashtagScore.merge(tag.trim(), weight, Double::sum);
                }
            }

            // 지역 (설계에 따라 지역 파싱 규칙은 바꿔도 됨)
            if (e.region() != null && !e.region().isBlank()) {
                String regionKey = normalizeRegion(e.region());
                regionScore.merge(regionKey, weight, Double::sum);
            }
        }

        int totalActions = viewCount + wishlistCount + reservationCount;
        int distinctPopupCount = popupAnyActionCount.size();
        long revisitPopupCount = popupAnyActionCount.values().stream()
                .filter(cnt -> cnt >= 2)
                .count();

        double revisitRate = distinctPopupCount == 0
                ? 0.0
                : (double) revisitPopupCount / distinctPopupCount;

        double nightRate = totalActions == 0 ? 0.0 : (double) nightActions / totalActions;
        double weekendRate = totalActions == 0 ? 0.0 : (double) weekendActions / totalActions;

        double priceFreeRatio = (freeCount + paidCount) == 0
                ? 0.0
                : (double) freeCount / (freeCount + paidCount);

        double avgGroupSize = groupVisitEvents == 0
                ? 0.0
                : (double) groupVisitTotalPeople / groupVisitEvents;

        // 상위 해시태그/지역 정렬
        List<UserPersonaTagStat> topHashtags = hashtagScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaTagStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<UserPersonaRegionStat> topRegions = regionScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaRegionStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        String periodLabel = buildPeriodLabel(minEventTime, maxEventTime);

        return new UserBehaviorSnapshot(
                userId,
                gender,
                ageInfo,
                viewCount,
                wishlistCount,
                reservationCount,
                distinctPopupCount,
                revisitRate,
                nightRate,
                weekendRate,
                priceFreeRatio,
                avgGroupSize,
                topHashtags,
                topRegions,
                periodLabel
        );
    }

    private String buildPeriodLabel(LocalDateTime minEventTime, LocalDateTime maxEventTime) {
        if (minEventTime == null || maxEventTime == null) {
            return "최근 활동 기준";
        }
        LocalDate start = minEventTime.toLocalDate();
        LocalDate end = maxEventTime.toLocalDate();
        if (start.equals(end)) {
            return start + " 하루 이용 기준";
        }
        return start + " ~ " + end;
    }

    private String normalizeRegion(String rawRegion) {
        if (rawRegion == null || rawRegion.isBlank()) {
            return "";
        }

        String[] parts = rawRegion.trim().split("\\s+");

        // 토큰이 2개 이상이면 "첫 번째 + 두 번째"
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }

        // 그 이하이면 있는 것만 그대로 반환
        return parts[0];
    }

    // -------------------------------------------------------
    // 3) 스냅샷 -> 육각형 축(점수) 계산
    // -------------------------------------------------------

    private List<UserPersonaAxis> buildHexagonAxesFromSnapshot(UserBehaviorSnapshot s) {

        // 0~100 사이 점수로 정규화 (대략적인 기준값)
        // 기준값, 공식은 마음대로 조정 가능

        // 1. 활동성: 전체 액션과 서로 다른 팝업 수 기반
        int totalActions = s.getTotalViewCount() + s.getTotalWishlistCount() + s.getTotalReservationCount();
        double activityScore = clamp(100.0 * totalActions / 50.0); // 50번 이상이면 만점 근처
        double explorationScore = clamp(100.0 * s.getDistinctPopupCount() / 20.0); // 20개 이상이면 만점 근처

        // 2. 탐색 다양성: 해시태그/지역의 다양성
        double hashtagVarietyScore = clamp(100.0 * s.getTopHashtags(999).size() / 15.0); // 15개 이상 쓰면 만점
        double regionVarietyScore = clamp(100.0 * s.getTopRegions(999).size() / 7.0);    // 7개 이상 돌아다니면 만점
        double exploration = (explorationScore * 0.5) + (hashtagVarietyScore * 0.3) + (regionVarietyScore * 0.2);

        // 3. 계획성: 예약 비율 + 야행성/주말 비율 역가중
        double reservationRatio = totalActions == 0 ? 0.0 : (double) s.getTotalReservationCount() / totalActions;
        double planScore = clamp(
                70.0 * reservationRatio      // 예약 많이 할수록 +
                        + 15.0 * (1.0 - s.getNightRate())  // 야행성 적을수록 +
                        + 15.0 * (1.0 - s.getWeekendRate()) // 주말 몰림 적을수록 +
        );

        // 4. 가격 민감도: 무료 비율 높을수록 "민감도"가 높다고 가정
        double priceSensitivityScore = clamp(100.0 * s.getPriceFreeRatio());

        // 5. 동행 선호(Social): 평균 인원 수로 계산 (1명 기준, 3명 이상이면 만점)
        double socialScore = clamp(100.0 * (s.getAvgGroupSize() - 1.0) / 2.0);

        // 6. 충성도(Loyalty): 재방문 팝업 비율
        double loyaltyScore = clamp(100.0 * s.getRevisitRate());

        List<UserPersonaAxis> axes = new ArrayList<>();

        axes.add(UserPersonaAxis.builder()
                .axisKey("ACTIVITY")
                .axisLabel("활동성")
                .score((int) Math.round(activityScore))
                .description("얼마나 자주 팝업을 보고, 찜하고, 예약하는지")
                .build());

        axes.add(UserPersonaAxis.builder()
                .axisKey("EXPLORATION")
                .axisLabel("탐색 다양성")
                .score((int) Math.round(exploration))
                .description("얼마나 다양한 팝업/해시태그/지역을 경험하는지")
                .build());

        axes.add(UserPersonaAxis.builder()
                .axisKey("PLAN")
                .axisLabel("계획성")
                .score((int) Math.round(planScore))
                .description("즉흥 방문보다, 미리 예약하고 계획적으로 움직이는 정도")
                .build());

        axes.add(UserPersonaAxis.builder()
                .axisKey("PRICE_SENSITIVITY")
                .axisLabel("가격 민감도")
                .score((int) Math.round(priceSensitivityScore))
                .description("무료/저가 팝업을 선호하는 정도")
                .build());

        axes.add(UserPersonaAxis.builder()
                .axisKey("SOCIAL")
                .axisLabel("동행 선호")
                .score((int) Math.round(socialScore))
                .description("혼자보다 여러 명과 함께 방문하는 비율")
                .build());

        axes.add(UserPersonaAxis.builder()
                .axisKey("LOYALTY")
                .axisLabel("재방문 성향")
                .score((int) Math.round(loyaltyScore))
                .description("마음에 든 팝업/분위기를 반복해서 찾는 경향")
                .build());

        return axes;
    }

    private double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    // -------------------------------------------------------
    // 4) LLM 호출해서 축 설명/요약 다듬기
    // -------------------------------------------------------

    private PersonaLlmResult callLlmForPersona(UserBehaviorSnapshot snapshot,
                                               List<UserPersonaAxis> axesDraft) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", snapshot.getUserId());
        payload.put("gender", snapshot.getGender());
        payload.put("age", snapshot.getAgeInfo().age());
        payload.put("ageGroupLabel", snapshot.getAgeInfo().detailLabel());
        payload.put("analysisPeriodLabel", snapshot.getAnalysisPeriodLabel());

        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("view", snapshot.getTotalViewCount());
        counts.put("wishlist", snapshot.getTotalWishlistCount());
        counts.put("reservation", snapshot.getTotalReservationCount());
        counts.put("distinctPopups", snapshot.getDistinctPopupCount());
        payload.put("counts", counts);

        Map<String, Object> ratios = new LinkedHashMap<>();
        ratios.put("revisitRate", snapshot.getRevisitRate());
        ratios.put("nightRate", snapshot.getNightRate());
        ratios.put("weekendRate", snapshot.getWeekendRate());
        ratios.put("priceFreeRatio", snapshot.getPriceFreeRatio());
        ratios.put("avgGroupSize", snapshot.getAvgGroupSize());
        payload.put("ratios", ratios);

        payload.put("axesDraft", axesDraft);
        payload.put("topHashtags", snapshot.getTopHashtags(5));
        payload.put("topRegions", snapshot.getTopRegions(5));

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }

        // ★ 여기부터 수정된 부분
        String prompt = """
            아래는 한 사용자의 팝업 이용 데이터 요약(JSON)입니다.
            
            이 정보를 기반으로:
            1) 이미 계산된 육각형 축(axesDraft)의 설명(description)을
               사람 친화적인 문장으로 다듬고,
            2) 전체 성향을 3~5문장 정도의 짧은 리포트로 요약해 주세요.
            
            주의사항:
            - 수치(score)는 그대로 유지합니다.
            - 축 이름(axisKey, axisLabel)은 변경하지 않습니다.
            - 요약은 해당 유저의 장점/패턴을 위주로 부드럽게 써 주세요.
            - 결과는 반드시 JSON 형식으로만 응답해 주세요.
            
            응답 JSON 스키마:
            {
              "axes": [
                {
                  "axisKey": "ACTIVITY",
                  "axisLabel": "활동성",
                  "score": 0-100,
                  "description": "한국어 설명..."
                },
                ...
              ],
              "summary": "한국어 요약 텍스트"
            }
            
            유저 데이터(JSON):
            %s
            """.formatted(json);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(PersonaLlmResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildFallbackSummary(UserBehaviorSnapshot s) {
        StringBuilder sb = new StringBuilder();

        sb.append("최근 이용 기록을 보면 ");
        if (s.getTotalReservationCount() > 0) {
            sb.append("예약을 활용해 계획적으로 방문하는 편이고, ");
        } else {
            sb.append("주로 가볍게 둘러보며 관심 있는 팝업을 골라보는 편이고, ");
        }

        if (!s.getTopHashtags(1).isEmpty()) {
            sb.append("특히 '")
                    .append(s.getTopHashtags(1).get(0).tag())
                    .append("' 관련 팝업에 관심이 많습니다. ");
        }

        if (!s.getTopRegions(1).isEmpty()) {
            sb.append("자주 찾는 지역은 ")
                    .append(s.getTopRegions(1).get(0).region())
                    .append(" 주변으로 나타납니다. ");
        }

        sb.append("전반적으로 자신의 취향에 맞는 팝업을 고르는 데에 확실한 기준이 있는 사용자입니다.");

        return sb.toString();
    }

    // -------------------------------------------------------
    // 5) 추천 로직
    // -------------------------------------------------------

    /**
     * 2번 요구사항:
     *  - "유저와 성향이 비슷한(유저가 찜한 해시태그가 들어있는 팝업을 보거나 찜했거나 예약한 적 있는)
     *    사람들이 찾는 팝업" (현재/예정 팝업 중에서)
     */
    private List<UserPersonaPopupCard> recommendBySimilarTaste(Long userId,
                                                               UserBehaviorSnapshot snapshot,
                                                               int limit) {

        // 내가 자주 쓰는 상위 해시태그들
        List<String> myTopTags = snapshot.getTopHashtags(5).stream()
                .map(UserPersonaTagStat::tag)
                .toList();

        if (myTopTags.isEmpty()) {
            return Collections.emptyList();
        }

        // PopupMapper.selectSimilarTastePopups(userId, limit) 쪽에서는
        // "사용자가 찜한 해시태그" 기준으로 이미 계산하고 있으니,
        // 굳이 tags를 넘기지 않는 버전으로 맞춘 상태라면 이대로 사용.
        return popupMapper.selectSimilarTastePopups(userId, limit);
    }

    /**
     * 3번 요구사항:
     *  - "유저의 성별과 연령대가 맞는 사람들이 많이 관심있어 하는 팝업"
     *  - birthYear 를 가지고 10년 단위 구간(예; 1990~1999)을 만들어서 Mapper에 넘김
     */
    private List<UserPersonaPopupCard> recommendByDemographic(String gender,
                                                              Integer birthYear,
                                                              int limit) {

        if (gender == null || birthYear == null || birthYear <= 0) {
            return Collections.emptyList();
        }

        int decadeStart = (birthYear / 10) * 10; // 1998 -> 1990
        int decadeEnd = decadeStart + 9;         // 1990 ~ 1999

        return popupMapper.selectDemographicPopularPopups(
                gender,
                decadeStart,
                decadeEnd,
                limit
        );
    }

    // -------------------------------------------------------
    // 내부용 record / DTO (Service 안에서만 사용)
    // -------------------------------------------------------

    /**
     * 유저 나이 정보
     */
    private record AgeInfo(
            Integer age,        // null 가능
            Integer group,      // 20, 30 ...
            String detailLabel, // "20대 초반"
            String groupLabel   // "20대"
    ) {
    }

    /**
     * 유저의 팝업 액션 타입
     */
    public enum UserPopupEventType {
        VIEW, WISHLIST, RESERVATION
    }

    /**
     * 유저의 개별 액션(조회/찜/예약) 이벤트
     * -> PopupMapper.selectUserPopupEvents 에서 가져온 DTO를 변환해서 사용
     */
    public record UserPopupEvent(
            Long popId,
            UserPopupEventType type,
            LocalDateTime timestamp,
            String priceType,            // FREE / PAID ...
            Integer peopleCount,         // 예약 인원 (VIEW/WISHLIST는 null)
            String region,               // POPUPSTORE.pop_location
            List<String> hashtags        // 이 팝업의 해시태그 이름들
    ) {
    }

    /**
     * 스냅샷: 유저의 행동 통계 집계 결과
     */
    @Getter
    private static class UserBehaviorSnapshot {

        private final Long userId;
        private final String gender;
        private final AgeInfo ageInfo;

        private final int totalViewCount;
        private final int totalWishlistCount;
        private final int totalReservationCount;

        private final int distinctPopupCount;
        private final double revisitRate;
        private final double nightRate;
        private final double weekendRate;
        private final double priceFreeRatio;
        private final double avgGroupSize;

        private final List<UserPersonaTagStat> topHashtags;   // 정렬된 리스트
        private final List<UserPersonaRegionStat> topRegions; // 정렬된 리스트

        private final String analysisPeriodLabel;

        private UserBehaviorSnapshot(Long userId,
                                     String gender,
                                     AgeInfo ageInfo,
                                     int totalViewCount,
                                     int totalWishlistCount,
                                     int totalReservationCount,
                                     int distinctPopupCount,
                                     double revisitRate,
                                     double nightRate,
                                     double weekendRate,
                                     double priceFreeRatio,
                                     double avgGroupSize,
                                     List<UserPersonaTagStat> topHashtags,
                                     List<UserPersonaRegionStat> topRegions,
                                     String analysisPeriodLabel) {

            this.userId = userId;
            this.gender = gender;
            this.ageInfo = ageInfo;
            this.totalViewCount = totalViewCount;
            this.totalWishlistCount = totalWishlistCount;
            this.totalReservationCount = totalReservationCount;
            this.distinctPopupCount = distinctPopupCount;
            this.revisitRate = revisitRate;
            this.nightRate = nightRate;
            this.weekendRate = weekendRate;
            this.priceFreeRatio = priceFreeRatio;
            this.avgGroupSize = avgGroupSize;
            this.topHashtags = topHashtags;
            this.topRegions = topRegions;
            this.analysisPeriodLabel = analysisPeriodLabel;
        }

        public static UserBehaviorSnapshot empty(Long userId, String gender, AgeInfo ageInfo) {
            return new UserBehaviorSnapshot(
                    userId,
                    gender,
                    ageInfo,
                    0, 0, 0,
                    0,
                    0.0, 0.0, 0.0,
                    0.0,
                    0.0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "아직 충분한 이용 기록이 없어 간단한 정보만 제공돼요."
            );
        }

        public List<UserPersonaTagStat> getTopHashtags(int limit) {
            if (topHashtags.size() <= limit) {
                return topHashtags;
            }
            return topHashtags.subList(0, limit);
        }

        public List<UserPersonaRegionStat> getTopRegions(int limit) {
            if (topRegions.size() <= limit) {
                return topRegions;
            }
            return topRegions.subList(0, limit);
        }
    }

    /**
     * LLM 응답 JSON 을 매핑할 DTO
     */
    public record PersonaLlmResult(
            List<UserPersonaAxis> axes,
            String summary
    ) {
    }
}
