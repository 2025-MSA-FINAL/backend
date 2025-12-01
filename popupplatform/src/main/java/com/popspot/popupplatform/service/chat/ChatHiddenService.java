package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatHidden;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatHiddenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Service
@RequiredArgsConstructor
public class ChatHiddenService {
    private final ChatHiddenMapper hiddenMapper;

    //채팅방 숨김처리
    @Transactional
    public void hideRoom(String type, Long roomId, Long userId) {
        //기존 숨김기록조회
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //숨김기록없을시 신규생성
        if(hidden == null) {
            ChatHidden newHidden = ChatHidden.builder()
                    .chType(type)
                    .chRoomId(roomId)
                    .userId(userId)
                    .chIsHidden(true)
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
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //숨김기록이 있을시 업데이트
        if(hidden == null) {
            throw new CustomException(ChatErrorCode.HIDDEN_RECORD_NOT_FOUND);
        }
        hiddenMapper.updateHiddenFlag(type,roomId,userId,false);
    }
    //숨김여부체크
    @Transactional
    public boolean isHidden(String type, Long roomId, Long userId) {
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        return hidden != null && Boolean.TRUE.equals(hidden.getChIsHidden());
    }
}
