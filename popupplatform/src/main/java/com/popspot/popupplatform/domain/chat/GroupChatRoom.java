package com.popspot.popupplatform.domain.chat;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatRoom {
    private Long gcrId; //그룹채팅방PK
    private Long popId; //팝업스토어PK
    private Long userId; //방장
    private Long cmId; //최근메세지
    private String gcrTitle; //그룹채팅방이름
    private String gcrDescription; //그룹채팅방설명
    private Integer gcrMaxUserCnt; //최대인원
    private String gcrLimitGender; //성별제한
    private Integer gcrMinAge; //최소입장나이
    private Integer gcrMaxAge; //최대입장나이
    private Boolean gcrIsDeleted; //삭제여부
}
