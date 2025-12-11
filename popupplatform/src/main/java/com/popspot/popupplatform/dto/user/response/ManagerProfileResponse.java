package com.popspot.popupplatform.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 프로필 정보 응답 DTO")
public class ManagerProfileResponse {

    @Schema(description = "유저 고유 ID", example = "10")
    private Long userId;

    @Schema(description = "매니저 실명", example = "나기현")
    private String name;

    @Schema(description = "브랜드명 (닉네임) - 수정 불가 필드", example = "팝스팟 스토어")
    private String brandName;

    @Schema(description = "이메일", example = "manager@popspot.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.popspot.com/profiles/1.jpg")
    private String profileImage;

    @Schema(description = "유저 역할", example = "MANAGER")
    private String role;
}