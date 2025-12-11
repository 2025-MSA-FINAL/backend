package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import com.popspot.popupplatform.mapper.chat.GroupChatRoomMapper;
import com.popspot.popupplatform.mapper.chat.PrivateChatRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {
    private final GroupChatRoomMapper groupChatRoomMapper;
    private final PrivateChatRoomMapper privateChatRoomMapper;
    private final ChatReadService chatReadService;
    private final ChatMessageMapper chatMessageMapper;

    //내가참여한방(1:1,그룹) 가져오기
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long userId) {
        List<ChatRoomSummaryResponse> groupRooms = groupChatRoomMapper.findGroupRoomsForUser(userId);
        List<ChatRoomSummaryResponse> privateRooms = privateChatRoomMapper.findPrivateRoomsForUser(userId);

        // 방마다 unreadCount 계산해서 추가
        groupRooms.forEach(room -> enrichRoom(room, userId));
        privateRooms.forEach(room -> enrichRoom(room, userId));

        //두 리스트 합쳐서 최신 메시지 시간 기준 내림차순 정렬
        return Stream.concat(groupRooms.stream(), privateRooms.stream()) //두개의 List를 스트림으로 변환 후 새로운 스트림 생성
                //내림차순(객체의createdAt기준으로비교-오름차순)으로 정렬
                .sorted(Comparator.comparing(ChatRoomSummaryResponse::getLatestMessageTime).reversed())
                .toList(); //정렬된 스트림을 리스트로 변환
    }

    // 특정 방의 unreadCount 계산
    private void addUnreadCount(ChatRoomSummaryResponse room, Long userId) {

        String roomType = room.getRoomType();   // "GROUP" / "PRIVATE"
        Long roomId = room.getRoomId();

        // Redis에서 마지막 읽은 메시지 ID 가져오기
        Long lastReadId = chatReadService.getLastRead(roomType, roomId, userId);

        // 해당 방에서 lastRead보다 큰 메시지 개수 조회
        int unreadCount = chatMessageMapper.countUnreadMessages(roomType, roomId, lastReadId);

        room.setUnreadCount(unreadCount); // DTO 설정
    }

    //최신 메세지 시간 조회 후 DTO 삽입
    private void enrichRoom(ChatRoomSummaryResponse room, Long userId) {
        addUnreadCount(room, userId);

        ChatMessageResponse lastest = chatMessageMapper.getLatestMessage(
                room.getRoomType(),
                room.getRoomId()
        );

        if(lastest != null) {
            room.setLatestMessageTime(lastest.getCreatedAt());
        } else  {
            //최근메세지 없으면 채팅방 생성기준
            room.setLatestMessageTime(room.getCreatedAt());
        }
    }
}