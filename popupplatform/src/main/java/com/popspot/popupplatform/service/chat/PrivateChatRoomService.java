package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.PrivateChatRoom;
import com.popspot.popupplatform.domain.chat.PrivateChatRoomDelete;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.PrivateChatRoomDeleteMapper;
import com.popspot.popupplatform.mapper.chat.PrivateChatRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrivateChatRoomService {
    private final PrivateChatRoomMapper privateChatRoomMapper;
    private final PrivateChatRoomDeleteMapper privateChatRoomDeleteMapper;

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
    //1대1채팅삭제
    @Transactional
    public void deletePrivateRoom(Long userId, Long pcrId) {
        PrivateChatRoom room = privateChatRoomMapper.findById(pcrId);
        //만약 pcrId에 해당하는 방이 없거나 이미 삭제된 방이라면 에러처리
        if(room == null || Boolean.TRUE.equals(room.getPcrIsDeleted())) {
            throw new CustomException(ChatErrorCode.PRIVATE_ROOM_NOT_FOUND);
        }
        //기존에 있던 삭제 기록 조회
        PrivateChatRoomDelete deleteRecord = privateChatRoomDeleteMapper.findDeleteRecord(pcrId, userId);
        //기존에 삭제했던 기록이 없다면
        if(deleteRecord == null) {
            //신규 생성
            PrivateChatRoomDelete newRecord = PrivateChatRoomDelete.builder()
                    .pcrId(pcrId)
                    .userId(userId)
                    .pcrdIsDeleted(true)
                    .lastDeletedAt(LocalDateTime.now())
                    .build();
            privateChatRoomDeleteMapper.insertDeleteRecord(newRecord);
            return;
        }
        //삭제여부가 TRUE면 이미 삭제되었다는 에러발생
        if(Boolean.TRUE.equals(deleteRecord.getPcrdIsDeleted())) {
            throw new CustomException(ChatErrorCode.PRIVATE_ROOM_ALREADY_DELETED);
        }
        //기존에 삭제했던 기록이 있다면 삭제했던 시간만 업데이트
        //삭제여부가 FALSE면 TRUE+LastDeletedAt 업데이트
        privateChatRoomDeleteMapper.updateDeleteFlag(pcrId, userId, true);
        privateChatRoomDeleteMapper.updateLastDeletedAt(pcrId, userId);
    }
    //1대1채팅삭제했는데, 새 메시지가 도착했을 때 (삭제상태해제 후, 방 재등장)
    @Transactional
    public void restorePrivateRoomOnNewMessage(Long userId, Long pcrId) {
        PrivateChatRoomDelete deleteRecord = privateChatRoomDeleteMapper.findDeleteRecord(pcrId, userId);
        //삭제 기록이 있고, 삭제여부가 true라면
        if(deleteRecord != null && Boolean.TRUE.equals(deleteRecord.getPcrdIsDeleted())) {
            //삭제여부만false, last_deleted_at은 유지
            privateChatRoomDeleteMapper.updateDeleteFlag(pcrId, userId, false);
        }
    }
    //해당UserId기준으로 해당채팅방PcrId가 삭제상태 여부확인
    @Transactional(readOnly = true)
    public boolean isDeletedForUser(Long userId, Long pcrId) {
        PrivateChatRoomDelete deleteRecord = privateChatRoomDeleteMapper.findDeleteRecord(pcrId, userId);
        return deleteRecord != null && Boolean.TRUE.equals(deleteRecord.getPcrdIsDeleted());
    }
    //해당UserId의 last_deleted_at 조회
    @Transactional(readOnly = true)
    public LocalDateTime getLastDeletedAt(Long userId, Long pcrId) {
        PrivateChatRoomDelete deleteRecord = privateChatRoomDeleteMapper.findDeleteRecord(pcrId, userId);
        return deleteRecord != null ? deleteRecord.getLastDeletedAt() : null;
    }
}
