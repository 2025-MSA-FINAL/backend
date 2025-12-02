package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatRoomHidden;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatRoomHiddenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//숨김/삭제가 한번이라도 있을 경우 들어감
@Service
@RequiredArgsConstructor
public class ChatRoomHiddenService {
    private final ChatRoomHiddenMapper hiddenMapper;

    //채팅방 숨김처리
    @Transactional
    public void hideRoom(String type, Long roomId, Long userId) {
        //기존 숨김기록조회
        ChatRoomHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //숨김기록없을시 신규생성
        if(hidden == null) {
            ChatRoomHidden newHidden = ChatRoomHidden.builder()
                    .crhType(type)
                    .crhRoomId(roomId)
                    .userId(userId)
                    .crhIsHidden(true)
                    .build();
            hiddenMapper.insertHidden(newHidden);
        } else {
            //기존숨김기록있을시 숨김기록업데이트
            hiddenMapper.updateHiddenFlag(type,roomId,userId,true);
        }
    }
    //채팅방 숨김해제
    @Transactional
    public void unhideRoom(String type, Long roomId, Long userId) {
        //기존 숨김기록조회
        ChatRoomHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //숨김기록이 없으면 에러 - 있으면 업데이트
        if(hidden == null) {
            throw new CustomException(ChatErrorCode.HIDDEN_RECORD_NOT_FOUND);
        }
        hiddenMapper.updateHiddenFlag(type,roomId,userId,false);
    }
    //숨김여부체크 (숨김했는지안했는지만)
    @Transactional(readOnly = true)
    public boolean isHidden(String type, Long roomId, Long userId) {
        ChatRoomHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        return hidden != null && Boolean.TRUE.equals(hidden.getCrhIsHidden());
    }
}
