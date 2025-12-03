package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
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

    //내가참여한방(1:1,그룹) 가져오기
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long userId) {
        List<ChatRoomSummaryResponse> groupRooms =  groupChatRoomMapper.findGroupRoomsForUser(userId);
        List<ChatRoomSummaryResponse> privateRooms = privateChatRoomMapper.findPrivateRoomsForUser(userId);
        //두 리스트 합쳐서 createdAt 내림차순 정렬
        return Stream.concat(groupRooms.stream(), privateRooms.stream()) //두개의 List를 스트림으로 변환 후 새로운 스트림 생성
                //내림차순(객체의createdAt기준으로비교-오름차순)으로 정렬
                .sorted(Comparator.comparing(ChatRoomSummaryResponse::getCreatedAt).reversed())
                .toList(); //정렬된 스트림을 리스트로 변환
    }
}
