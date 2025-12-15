package com.popspot.popupplatform.service.main;

import com.popspot.popupplatform.dto.main.MainRecommendPopupDto;
import com.popspot.popupplatform.dto.main.MainRecommendResponse;
import com.popspot.popupplatform.dto.main.UserTasteProfileDto;
import com.popspot.popupplatform.global.utils.JsonUtil;
import com.popspot.popupplatform.mapper.main.MainRecommendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MainRecommendServiceImpl implements MainRecommendService {

    private final MainRecommendMapper mapper;
    private final ChatClient chatClient;

    public MainRecommendServiceImpl(
            MainRecommendMapper mapper,
            ChatClient.Builder builder
    ) {
        this.mapper = mapper;
        this.chatClient = builder
                .defaultSystem("""
                    너는 팝업스토어 추천 엔진이다.
                    입력으로 유저 취향과 후보 팝업 목록이 주어진다.
                    반드시 JSON 배열로 popId만 출력한다.
                    예: [7101,7102,7103]
                    다른 텍스트는 절대 출력하지 마라.
                """)
                .build();
    }

    @Override
    public MainRecommendResponse getRecommendedPopups(Long userId, int limit) {

        System.out.println(userId+"============");
        /* =========================
           1) 비로그인 → 인기 추천
           ========================= */
        if (userId == null) {
            return MainRecommendResponse.builder()
                    .type("POPULAR")
                    .items(mapper.selectPopularPopups(limit))
                    .build();
        }

        /* =========================
           2) 로그인 → AI 추천
           ========================= */

        // (1) 유저 취향 프로필
        UserTasteProfileDto profile =
                mapper.selectUserTasteProfile(userId);

        // (2) 후보군 (최대 50개)
        List<MainRecommendPopupDto> candidates =
                mapper.selectAiCandidates(userId, 50);

        // 데이터 부족 → fallback
        //todo : 여기 데이터 부족해서 거의 무조건 null 나와서 로그인해도  POPULAR로 나옴 어짜피 해시태그 값 DTO에 안주는데 prompt에는 해시태그
        //todo : 던져주고 있는데 비교도 못하는데 던져주고 있어서  dto에 추가하던 애초에 로직을 변경해야하는 상황임 여기 수정해야함
        if (candidates == null || candidates.isEmpty()) {
            return MainRecommendResponse.builder()
                    .type("POPULAR")
                    .items(mapper.selectPopularPopups(limit))
                    .build();
        }

        try {
            String prompt = buildPrompt(profile, candidates, limit);

            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            List<Long> orderedIds = JsonUtil.parseLongArray(content);

            if (orderedIds == null || orderedIds.isEmpty()) {
                throw new IllegalStateException("AI empty result");
            }

            List<MainRecommendPopupDto> result =
                    mapper.selectPopupsByIdsInOrder(orderedIds);

            return MainRecommendResponse.builder()
                    .type("AI")
                    .items(result)
                    .build();

        } catch (Exception e) {
            // AI 실패 → 인기 fallback
            return MainRecommendResponse.builder()
                    .type("POPULAR")
                    .items(mapper.selectPopularPopups(limit))
                    .build();
        }
    }

    /* =========================
       ChatClient 프롬프트 생성
       ========================= */
    private String buildPrompt(
            UserTasteProfileDto profile,
            List<MainRecommendPopupDto> candidates,
            int limit
    ) {
        return """
            [유저 취향]
            - 최근 본: %s
            - 선호 태그: %s
            - 가격 선호: %s

            [후보 팝업 목록]
            %s

            위 후보 중에서 %d개를 추천하라.
            조건:
            1) 유저 취향과 유사할수록 우선
            2) 같은 테마만 몰리지 않게 다양성 고려
            3) 반드시 JSON 배열로 popId만 출력
            """
                .formatted(
                        profile != null ? profile.getRecentViewedNames() : "없음",
                        profile != null ? profile.getTopTags() : "없음",
                        profile != null ? profile.getPricePreference() : "MIXED",
                        candidatesToText(candidates),
                        limit
                );
    }

    private String candidatesToText(List<MainRecommendPopupDto> list) {
        StringBuilder sb = new StringBuilder();
        for (MainRecommendPopupDto p : list) {
            sb.append("""
                - {popId:%d, name:%s, location:%s, price:%s}
                """.formatted(
                    p.getPopId(),
                    p.getPopName(),
                    p.getPopLocation(),
                    p.getPopPriceType()
            ));
        }
        return sb.toString();
    }
}