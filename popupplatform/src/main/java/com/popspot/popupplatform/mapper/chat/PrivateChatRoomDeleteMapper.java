package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.PrivateChatRoomDelete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrivateChatRoomDeleteMapper {
    //특정유저userId가 특정1대1방pcrId에 대한 삭제 기록 조회
    PrivateChatRoomDelete findDeleteRecord(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId
    );
    //삭제기록생성
    void insertDeleteRecord(PrivateChatRoomDelete delete);
    //삭제기록플래그수정(삭제설정/해제라고보면됨)
    void updateDeleteFlag(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId,
            @Param("isDeleted") Boolean isDeleted
    );
    //삭제시점갱신(다시삭제할때)
    void updateLastDeletedAt(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId
    );
}
