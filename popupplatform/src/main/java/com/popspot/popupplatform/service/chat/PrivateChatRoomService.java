package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.PrivateChatRoom;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatHiddenMapper;
import com.popspot.popupplatform.mapper.chat.PrivateChatRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivateChatRoomService {
    private final PrivateChatRoomMapper privateChatRoomMapper;
    private final ChatHiddenMapper chatHiddenMapper;

    private static final String TYPE_PRIVATE = "PRIVATE";

    //1:1채팅시작
    //현재로그인유저currentUserId, 채팅하는상대방targetUserId
    //자신에게도 채팅 가능
    public Long startPrivateChat(Long currentUserId, Long targetUserId){
        //양방향으로 기존 방이 있는지 확인
        PrivateChatRoom room = privateChatRoomMapper.findActiveRoomByUsers(currentUserId, targetUserId);
        //기존 방이 있으면 그대로 반환 - 숨김은 그대로. 따로 풀어줘야함.
        if(room!=null) {
            return room.getPcrId();
        }
        //기존 방이 없으면 새로 방 생성
        PrivateChatRoom newRoom = PrivateChatRoom.builder()
                .userId(currentUserId)
                .userId2(targetUserId)
                .pcrIsDeleted(false)
                .build();
        try {
            privateChatRoomMapper.insertRoom(newRoom);
        } catch (Exception e) {
            throw new CustomException(ChatErrorCode.PRIVATE_ROOM_NOT_FOUND);
        }
        return newRoom.getPcrId();
    }
}
