package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatHidden;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatHiddenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

//숨김/삭제가 한번이라도 있을 경우 들어감
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
                    .chIsDeleted(false)
                    .lastDeletedAt(null)
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
        //숨김기록이 없으면 에러 - 있으면 업데이트
        if(hidden == null) {
            throw new CustomException(ChatErrorCode.HIDDEN_RECORD_NOT_FOUND);
        }
        hiddenMapper.updateHiddenFlag(type,roomId,userId,false);
    }
    //채팅방 삭제
    @Transactional
    public void deleteChat(String type, Long roomId, Long userId) {
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //삭제기록없을시 신규생성
        if(hidden==null) {
            hiddenMapper.insertHidden(ChatHidden.builder()
                            .chType(type)
                            .chRoomId(roomId)
                            .userId(userId)
                            .chIsHidden(false)
                            .chIsDeleted(true)
                            .lastDeletedAt(java.time.LocalDateTime.now())
                            .build());
        } else {
            //지난 삭제 기록이 있고 다시 재삭제할 경우 시간 update
            hiddenMapper.updateHiddenFlag(type, roomId, userId, false);
            hiddenMapper.updateDeleteInfo(type, roomId, userId, true);
            hiddenMapper.updateLastDeletedAt(type, roomId, userId);
        }
    }
    //새로운 메시지가 오면 삭제 자동 해제(false)
    @Transactional
    public void restoreOnMessage(String type, Long roomId, Long userId) {
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        //hidden 기록이 있고, 이미 삭제 상태라면
        if(hidden != null && Boolean.TRUE.equals(hidden.getChIsDeleted())) {
            hiddenMapper.updateDeleteInfo(type, roomId, userId, false);
        }
    }

    //숨김여부체크 (숨김했는지안했는지만)
    @Transactional(readOnly = true)
    public boolean isHidden(String type, Long roomId, Long userId) {
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        return hidden != null && Boolean.TRUE.equals(hidden.getChIsHidden());
    }
    //삭제여부체크
    @Transactional(readOnly = true)
    public boolean isDeleted(String type, Long roomId, Long userId) {
        ChatHidden hidden = hiddenMapper.findHidden(type, roomId, userId);
        return hidden != null && Boolean.TRUE.equals(hidden.getChIsDeleted());
    }
    //숨김전체정보
    @Transactional(readOnly = true)
    public ChatHidden getHiddenInfo(String type, Long roomId, Long userId) {
        return hiddenMapper.findHidden(type, roomId, userId);
    }

}
