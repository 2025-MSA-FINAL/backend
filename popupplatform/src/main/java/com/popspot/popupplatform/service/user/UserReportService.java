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

    // 후보/추천 개수 고정
    private static final int RECO_CANDIDATE_LIMIT = 60;
    private static final int RECO_FINAL_LIMIT = 6;

    public UserReportService(ChatClient.Builder builder,
                             UserMapper userMapper,
                             PopupMapper popupMapper) {

        this.userMapper = userMapper;
        this.popupMapper = popupMapper;

        // ✅ 유저 성향 한마디+상세 / 추천 이유 생성만 수행
        this.chatClient = builder
                .defaultSystem("""
                        너는 팝업 스토어 플랫폼의 '리포트 카피라이터'다.
                        - 입력으로 유저 행동 요약(JSON)과, 서버가 확정한 추천 팝업 목록(JSON)이 들어온다.
                        - 너의 역할은:
                          1) 유저 성향을 "한마디(personaOneLiner)" + "상세 설명(personaDetail)"로 작성
                          2) 추천 팝업들의 "이유(reasons)"를 1~2개씩 작성
                        - 주의:
                          - 추천 대상(popId), 추천 순서는 절대 바꾸지 않는다.
                          - 없는 정보를 지어내지 않는다(추측 금지).
                          - 이유 라벨(label)은 아래 중 하나로만 사용:
                            ["해시태그","지역","연령대","성별","가격","진행상태","인기"]
                          - 결과는 반드시 JSON 형식으로만 응답한다.
                        - 모든 응답은 한국어로 작성한다.
                        """)
                .build();
    }

    /**
     * ✅ 유저 리포트 (3개만)
     * 1) 유저 성향 분석 (LLM)
     * 2) 자주 가는 지역/해시태그
     * 3) 추천(서버 고정) + 이유(LLM)
     */
    public UserPersonaReport userReport(Long userId) {

        // 유저 기본 정보(성별/출생년도) - 추천 후보 demographic에만 사용
        UserLimitInfoDto limitInfo = userMapper.findUserLimitInfo(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 또는 비활성화된 유저입니다. userId=" + userId));

        String gender = limitInfo.getUserGender();         // "M" / "F" / null
        Integer birthYear = limitInfo.getUserBirthyear();  // 1998 등
        AgeInfo ageInfo = calculateAgeInfo(birthYear);

        // 유저 행동 이벤트
        List<UserPopupEventDto> eventDtos = popupMapper.selectUserPopupEvents(userId);
        if (eventDtos == null) eventDtos = Collections.emptyList();

        // 스냅샷(해시태그/지역 TOP 포함)
        UserBehaviorSnapshot snapshot = buildBehaviorSnapshot(userId, gender, ageInfo, eventDtos);

        // 2) 자주 가는 지역/해시태그
        List<UserPersonaTagStat> topHashtags = snapshot.getTopHashtags(8);
        List<UserPersonaRegionStat> topRegions = snapshot.getTopRegions(8);

        // 3) 추천(서버 고정) + 이유(LLM)
        List<UserRecommendedPopupCard> recommendations =
                buildDeterministicRecommendations(userId, snapshot, gender, birthYear);

        // 1) 유저 성향 분석(LLM)
        PersonaAndReasonsResult llmResult =
                callLlmForPersonaAndReasons(snapshot, topHashtags, topRegions, recommendations);

        String personaOneLiner = (llmResult != null && llmResult.personaOneLiner != null && !llmResult.personaOneLiner.isBlank())
                ? llmResult.personaOneLiner
                : buildFallbackPersonaOneLiner(snapshot);

        String personaDetail = (llmResult != null && llmResult.personaDetail != null && !llmResult.personaDetail.isBlank())
                ? llmResult.personaDetail
                : buildFallbackPersonaDetail(snapshot);

        // LLM reasons 적용(실패 시 서버 fallback reasons 유지)
        if (llmResult != null && llmResult.recommendationReasons != null && !llmResult.recommendationReasons.isEmpty()) {
            applyLlmReasonsToRecommendations(recommendations, llmResult.recommendationReasons);
        }

        return UserPersonaReport.builder()
                .personaOneLiner(personaOneLiner)
                .personaDetail(personaDetail)
                .topHashtags(topHashtags)
                .topRegions(topRegions)
                .recommendations(recommendations)
                .build();
    }

    // -------------------------------------------------------
    // ✅ 1) 유저 성향 분석 + 추천 이유를 한 번에 받기
    // -------------------------------------------------------

    public static class PersonaAndReasonsResult {
        public String personaOneLiner;
        public String personaDetail;

        // popId -> reasons
        public List<RecoReasonItem> recommendationReasons;
    }

    public static class RecoReasonItem {
        public Long popId;
        public List<UserRecommendationReason> reasons;
    }

    private PersonaAndReasonsResult callLlmForPersonaAndReasons(UserBehaviorSnapshot snapshot,
                                                                List<UserPersonaTagStat> topHashtags,
                                                                List<UserPersonaRegionStat> topRegions,
                                                                List<UserRecommendedPopupCard> recommendations) {

        Map<String, Object> payload = new LinkedHashMap<>();

        Map<String, Object> behavior = new LinkedHashMap<>();
        behavior.put("totalViewCount", snapshot.getTotalViewCount());
        behavior.put("totalWishlistCount", snapshot.getTotalWishlistCount());
        behavior.put("totalReservationCount", snapshot.getTotalReservationCount());
        behavior.put("distinctPopupCount", snapshot.getDistinctPopupCount());
        behavior.put("revisitRate", snapshot.getRevisitRate());
        behavior.put("nightRate", snapshot.getNightRate());
        behavior.put("weekendRate", snapshot.getWeekendRate());
        behavior.put("priceFreeRatio", snapshot.getPriceFreeRatio());
        behavior.put("avgGroupSize", snapshot.getAvgGroupSize());

        payload.put("behavior", behavior);
        payload.put("topHashtags", topHashtags.stream().limit(5).toList());
        payload.put("topRegions", topRegions.stream().limit(5).toList());

        // 추천은 "서버가 이미 확정"된 결과만 전달
        List<Map<String, Object>> recos = new ArrayList<>();
        for (UserRecommendedPopupCard r : recommendations) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("popId", r.getPopId());
            m.put("title", r.getTitle());
            m.put("location", r.getLocation());
            m.put("priceType", r.getPriceType());
            m.put("price", r.getPrice());
            m.put("status", r.getStatus());
            m.put("serverScore", r.getServerScore());
            m.put("matchLevel", r.getMatchLevel());
            recos.add(m);
        }
        payload.put("recommendations", recos);

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }

        String prompt = """
                아래 JSON은 유저 행동 요약과, 서버가 확정한 추천 팝업 목록입니다.
                
                1) 유저 성향을 다음 형식으로 작성:
                   - personaOneLiner: 20자~35자 정도의 한마디(너무 과장 X)
                   - personaDetail: 2~4문장 상세 설명
                     * 반드시 근거 라벨을 문장 안에 자연스럽게 포함해 주세요.
                     * 예: [해시태그] ... [지역] ... [예약] ...
                
                2) 추천 팝업(recommendations) 각각에 대해 reasons를 1~2개 작성:
                   - label은 ["해시태그","지역","연령대","성별","가격","진행상태","인기"] 중 하나
                   - 없는 정보 추측 금지
                   - popId/순서 변경 금지
                
                응답 JSON 스키마:
                {
                  "personaOneLiner": "...",
                  "personaDetail": "...",
                  "recommendationReasons": [
                    {
                      "popId": 123,
                      "reasons": [
                        {"label":"해시태그","text":"..."},
                        {"label":"지역","text":"..."}
                      ]
                    }
                  ]
                }
                
                입력 JSON:
                %s
                """.formatted(json);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(PersonaAndReasonsResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyLlmReasonsToRecommendations(List<UserRecommendedPopupCard> recommendations,
                                                  List<RecoReasonItem> reasonItems) {
        if (recommendations == null || recommendations.isEmpty()) return;
        if (reasonItems == null || reasonItems.isEmpty()) return;

        Map<Long, List<UserRecommendationReason>> map = new HashMap<>();
        for (RecoReasonItem item : reasonItems) {
            if (item == null || item.popId == null) continue;
            if (item.reasons == null) continue;

            List<UserRecommendationReason> clean = item.reasons.stream()
                    .filter(r -> r != null
                            && r.getLabel() != null && !r.getLabel().isBlank()
                            && r.getText() != null && !r.getText().isBlank())
                    .limit(2)
                    .toList();

            if (!clean.isEmpty()) {
                map.put(item.popId, clean);
            }
        }

        for (UserRecommendedPopupCard r : recommendations) {
            List<UserRecommendationReason> reasons = map.get(r.getPopId());
            if (reasons != null && !reasons.isEmpty()) {
                r.setReasons(reasons);
            }
        }
    }

    private String buildFallbackPersonaOneLiner(UserBehaviorSnapshot s) {
        // 데이터 없으면 기본값
        int total = s.getTotalViewCount() + s.getTotalWishlistCount() + s.getTotalReservationCount();
        if (total == 0) return "아직은 취향을 모으는 탐색 중";

        if (s.getTotalReservationCount() > 0 && s.getRevisitRate() >= 0.5) return "취향 확실한 계획형 방문러";
        if (s.getTotalReservationCount() > 0) return "마음에 들면 바로 예약하는 타입";
        if (s.getTotalWishlistCount() > s.getTotalViewCount()) return "찜으로 꼼꼼히 추리는 타입";
        return "가볍게 둘러보다 꽂히면 움직이는 타입";
    }

    private String buildFallbackPersonaDetail(UserBehaviorSnapshot s) {
        StringBuilder sb = new StringBuilder();

        int total = s.getTotalViewCount() + s.getTotalWishlistCount() + s.getTotalReservationCount();
        if (total == 0) {
            sb.append("아직 충분한 이용 기록이 없어 성향 분석이 간단하게 제공돼요. ");
            sb.append("[해시태그] 관심 키워드를 조금 더 모으면 추천 정확도가 올라가요.");
            return sb.toString();
        }

        sb.append("[활동] 최근 기록을 보면 조회 ")
                .append(s.getTotalViewCount())
                .append("회, 찜 ")
                .append(s.getTotalWishlistCount())
                .append("회, 예약 ")
                .append(s.getTotalReservationCount())
                .append("회로 나타나요. ");

        if (!s.getTopHashtags(1).isEmpty()) {
            sb.append("[해시태그] '").append(s.getTopHashtags(1).get(0).tag()).append("' 관련 콘텐츠를 자주 봐요. ");
        }
        if (!s.getTopRegions(1).isEmpty()) {
            sb.append("[지역] ").append(s.getTopRegions(1).get(0).region()).append(" 주변 방문이 많아요. ");
        }

        if (s.getTotalReservationCount() > 0) sb.append("[예약] 마음에 들면 실제 방문까지 이어지는 편이에요.");
        else sb.append("[예약] 아직은 저장/탐색 위주로 취향을 고르는 단계예요.");

        return sb.toString();
    }

    // -------------------------------------------------------
    // ✅ 3) 추천: 서버가 후보군/점수/정렬 확정 (Deterministic)
    // -------------------------------------------------------

    private List<UserRecommendedPopupCard> buildDeterministicRecommendations(Long userId,
                                                                             UserBehaviorSnapshot snapshot,
                                                                             String gender,
                                                                             Integer birthYear) {

        Integer decadeStart = null;
        Integer decadeEnd = null;
        if (birthYear != null && birthYear > 0) {
            decadeStart = (birthYear / 10) * 10;
            decadeEnd = decadeStart + 9;
        }

        List<UserRecommendationCandidateDto> candidates =
                popupMapper.selectUserReportRecommendationCandidates(
                        userId,
                        gender,
                        decadeStart,
                        decadeEnd,
                        RECO_CANDIDATE_LIMIT
                );

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> myTopTags = snapshot.getTopHashtags(8).stream()
                .map(UserPersonaTagStat::tag)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> myTopRegions = snapshot.getTopRegions(5).stream()
                .map(UserPersonaRegionStat::region)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int maxPopularity = candidates.stream()
                .map(UserRecommendationCandidateDto::getPopularityScore)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        int maxDemographic = candidates.stream()
                .map(UserRecommendationCandidateDto::getDemographicScore)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        List<ScoredReco> scored = new ArrayList<>();

        for (UserRecommendationCandidateDto c : candidates) {

            int tagOverlap = countTagOverlap(myTopTags, c.getHashtagsCsv());
            int regionMatch = isRegionMatch(myTopRegions, c.getRegion()) ? 1 : 0;

            double tagScore = myTopTags.isEmpty()
                    ? 0.0
                    : clamp01((double) tagOverlap / Math.min(3, myTopTags.size()));

            double popularityScore = maxPopularity == 0 ? 0.0 : clamp01((double) nz(c.getPopularityScore()) / maxPopularity);

            double demographicScore = (gender == null || decadeStart == null)
                    ? 0.0
                    : (maxDemographic == 0 ? 0.0 : clamp01((double) nz(c.getDemographicScore()) / maxDemographic));

            double statusBoost = statusBoost(c.getStatus());

            // ✅ 서버 고정 점수(0~100)
            int finalScore = (int) Math.round(
                    55.0 * tagScore
                            + 15.0 * regionMatch
                            + 12.0 * demographicScore
                            + 10.0 * popularityScore
                            + 8.0 * statusBoost
            );
            finalScore = (int) clamp(finalScore);

            String matchLevel = (finalScore >= 75) ? "HIGH" : (finalScore >= 50 ? "MID" : "LOW");

            // LLM 실패 대비 서버 fallback reasons
            List<UserRecommendationReason> fallbackReasons = buildFallbackRecoReasons(snapshot, c, tagOverlap, regionMatch);

            UserRecommendedPopupCard card = UserRecommendedPopupCard.builder()
                    .popId(c.getPopId())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .title(c.getTitle())
                    .location(c.getLocation())
                    .price(c.getPrice())
                    .priceType(c.getPriceType())
                    .status(c.getStatus())
                    .serverScore(finalScore)
                    .matchLevel(matchLevel)
                    .reasons(fallbackReasons)
                    .build();

            scored.add(new ScoredReco(finalScore, c.getPopId(), card));
        }

        // ✅ 결정적 정렬: score DESC, popId DESC
        scored.sort((a, b) -> {
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            return Long.compare(b.popId, a.popId);
        });

        return scored.stream()
                .limit(RECO_FINAL_LIMIT)
                .map(s -> s.card)
                .collect(Collectors.toList());
    }

    private record ScoredReco(int score, long popId, UserRecommendedPopupCard card) {}

    private List<UserRecommendationReason> buildFallbackRecoReasons(UserBehaviorSnapshot snapshot,
                                                                    UserRecommendationCandidateDto c,
                                                                    int tagOverlap,
                                                                    int regionMatch) {
        List<UserRecommendationReason> reasons = new ArrayList<>();

        if (tagOverlap > 0) {
            reasons.add(new UserRecommendationReason("해시태그", "최근 관심 태그와 겹치는 키워드가 있어요."));
        }
        if (regionMatch == 1) {
            reasons.add(new UserRecommendationReason("지역", "자주 찾는 지역과 동선이 비슷해요."));
        }

        if (reasons.isEmpty()) {
            if ("FREE".equalsIgnoreCase(c.getPriceType())) {
                reasons.add(new UserRecommendationReason("가격", "부담 없이 가볍게 들르기 좋아요."));
            } else {
                reasons.add(new UserRecommendationReason("인기", "최근 관심도가 높은 팝업 중 하나예요."));
            }
        }

        if (reasons.size() > 2) return reasons.subList(0, 2);
        return reasons;
    }

    private int countTagOverlap(Set<String> myTopTags, String hashtagsCsv) {
        if (myTopTags == null || myTopTags.isEmpty()) return 0;
        if (hashtagsCsv == null || hashtagsCsv.isBlank()) return 0;

        Set<String> tags = Arrays.stream(hashtagsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        int overlap = 0;
        for (String t : myTopTags) {
            if (tags.contains(t)) overlap++;
        }
        return overlap;
    }

    private boolean isRegionMatch(Set<String> myTopRegions, String regionKey) {
        if (myTopRegions == null || myTopRegions.isEmpty()) return false;
        if (regionKey == null || regionKey.isBlank()) return false;
        return myTopRegions.contains(regionKey.trim());
    }

    private double statusBoost(String status) {
        if (status == null) return 0.0;
        return switch (status) {
            case "ONGOING" -> 1.0;
            case "UPCOMING" -> 0.6;
            default -> 0.0;
        };
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    // -------------------------------------------------------
    // ✅ 나이/연령대(추천 demographic용)
    // -------------------------------------------------------

    private AgeInfo calculateAgeInfo(Integer birthYear) {
        if (birthYear == null || birthYear <= 0) {
            return new AgeInfo(null, null, null, "연령 정보 없음");
        }

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear + 1;

        int group = (age / 10) * 10;
        String groupLabel = group + "대";

        String detailLabel;
        int mod = age % 10;
        if (mod <= 3) detailLabel = groupLabel + " 초반";
        else if (mod <= 6) detailLabel = groupLabel + " 중반";
        else detailLabel = groupLabel + " 후반";

        return new AgeInfo(age, group, detailLabel, groupLabel);
    }

    // -------------------------------------------------------
    // ✅ 행동 스냅샷(지역/해시태그 TOP 만들기 위해 유지)
    // -------------------------------------------------------

    private UserBehaviorSnapshot buildBehaviorSnapshot(Long userId,
                                                       String gender,
                                                       AgeInfo ageInfo,
                                                       List<UserPopupEventDto> eventDtos) {

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

        Map<Long, Integer> popupAnyActionCount = new HashMap<>();
        Map<String, Double> hashtagScore = new HashMap<>();
        Map<String, Double> regionScore = new HashMap<>();

        int freeCount = 0;
        int paidCount = 0;

        int groupVisitTotalPeople = 0;
        int groupVisitEvents = 0;

        int nightActions = 0;
        int weekendActions = 0;

        for (UserPopupEvent e : events) {

            switch (e.type()) {
                case VIEW -> viewCount++;
                case WISHLIST -> wishlistCount++;
                case RESERVATION -> reservationCount++;
            }

            popupAnyActionCount.merge(e.popId(), 1, Integer::sum);

            if ("FREE".equalsIgnoreCase(e.priceType())) freeCount++;
            else if ("PAID".equalsIgnoreCase(e.priceType())) paidCount++;

            if (e.type() == UserPopupEventType.RESERVATION && e.peopleCount() != null) {
                groupVisitTotalPeople += e.peopleCount();
                groupVisitEvents++;
            }

            int hour = e.timestamp().getHour();
            if (hour >= 20 || hour < 6) nightActions++;

            DayOfWeek dow = e.timestamp().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) weekendActions++;

            double weight = switch (e.type()) {
                case VIEW -> 1.0;
                case WISHLIST -> 2.0;
                case RESERVATION -> 3.0;
            };

            if (e.hashtags() != null) {
                for (String tag : e.hashtags()) {
                    if (tag == null || tag.isBlank()) continue;
                    hashtagScore.merge(tag.trim(), weight, Double::sum);
                }
            }

            if (e.region() != null && !e.region().isBlank()) {
                String regionKey = normalizeRegion(e.region());
                regionScore.merge(regionKey, weight, Double::sum);
            }
        }

        int totalActions = viewCount + wishlistCount + reservationCount;
        int distinctPopupCount = popupAnyActionCount.size();

        double revisitRate;
        if (hashtagScore.isEmpty()) {
            revisitRate = 0.0;
        } else {
            List<Double> sortedTagScores = hashtagScore.values().stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            double totalTagScore = sortedTagScores.stream().mapToDouble(Double::doubleValue).sum();

            int topK = Math.min(3, sortedTagScores.size());
            double topKScore = 0.0;
            for (int i = 0; i < topK; i++) topKScore += sortedTagScores.get(i);

            revisitRate = (totalTagScore == 0.0) ? 0.0 : (topKScore / totalTagScore);
        }

        double nightRate = totalActions == 0 ? 0.0 : (double) nightActions / totalActions;
        double weekendRate = totalActions == 0 ? 0.0 : (double) weekendActions / totalActions;

        double priceFreeRatio = (freeCount + paidCount) == 0 ? 0.0 : (double) freeCount / (freeCount + paidCount);
        double avgGroupSize = groupVisitEvents == 0 ? 0.0 : (double) groupVisitTotalPeople / groupVisitEvents;

        List<UserPersonaTagStat> topHashtags = hashtagScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaTagStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<UserPersonaRegionStat> topRegions = regionScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaRegionStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

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
                topRegions
        );
    }

    private String normalizeRegion(String rawRegion) {
        if (rawRegion == null || rawRegion.isBlank()) return "";
        String[] parts = rawRegion.trim().split("\\s+");
        if (parts.length >= 2) return parts[0] + " " + parts[1];
        return parts[0];
    }

    // -------------------------------------------------------
    // 내부용 record/DTO
    // -------------------------------------------------------

    private record AgeInfo(
            Integer age,
            Integer group,
            String detailLabel,
            String groupLabel
    ) {}

    public enum UserPopupEventType {
        VIEW, WISHLIST, RESERVATION
    }

    public record UserPopupEvent(
            Long popId,
            UserPopupEventType type,
            LocalDateTime timestamp,
            String priceType,
            Integer peopleCount,
            String region,
            List<String> hashtags
    ) {}

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

        private final List<UserPersonaTagStat> topHashtags;
        private final List<UserPersonaRegionStat> topRegions;

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
                                     List<UserPersonaRegionStat> topRegions) {

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
                    Collections.emptyList()
            );
        }

        public List<UserPersonaTagStat> getTopHashtags(int limit) {
            if (topHashtags.size() <= limit) return topHashtags;
            return topHashtags.subList(0, limit);
        }

        public List<UserPersonaRegionStat> getTopRegions(int limit) {
            if (topRegions.size() <= limit) return topRegions;
            return topRegions.subList(0, limit);
        }
    }
}
