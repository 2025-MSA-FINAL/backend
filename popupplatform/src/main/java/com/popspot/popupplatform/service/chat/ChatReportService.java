package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.enums.ReportType;
import com.popspot.popupplatform.dto.chat.request.ChatReportRequest;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.mapper.chat.GroupChatRoomMapper;
import com.popspot.popupplatform.mapper.report.ReportMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReportService {
    private final ReportMapper reportMapper;
    private final GroupChatRoomMapper groupChatRoomMapper;
    private final UserMapper userMapper;

    //신고생성insert
    @Transactional
    public Long report(ChatReportRequest req, CustomUserDetails user) {
        //이미지 필수
        if (req.getImageUrls() == null || req.getImageUrls().isEmpty()) {
            throw new CustomException(ChatErrorCode.IMAGE_REQUIRED);
        }
        //신고하는사람ID
        req.setUserId(user.getUserId());
        //신고타입
        ReportType reportType  = req.getReportType();
        //신고타입이 없을 경우
        if (reportType == null) {
            throw new CustomException(ChatErrorCode.INVALID_REPORT_TYPE);
        }
        //신고대상존재여부
        validateTarget(reportType, req.getTargetId(), user.getUserId());
        //중복 신고
        boolean isDuplicate = reportMapper.existsPendingReport(
                user.getUserId(),
                reportType.name(),
                req.getTargetId()
        );
        if (isDuplicate) {
            throw new CustomException(ChatErrorCode.DUPLICATE_REPORT);
        }
        //신고 저장
        reportMapper.insertReport(req);
        Long repId = req.getRepId(); //방금 생성된 reqId 불러오기
        //신고이미지 저장
        for(String url : req.getImageUrls()) {
            reportMapper.insertReportImage(repId, url);
        }
        return repId;
    }
    //신고대상존재여부
    private void validateTarget(ReportType type, Long targetId, Long reporterId) {
        //신고대상이 없을 경우
        if(targetId == null) {
            throw new CustomException(ChatErrorCode.INVALID_REPORT_TARGET);
        }
        switch (type) {
            case CHAT -> {
                if(groupChatRoomMapper.findById(targetId) == null) { //신고대상(채팅방)이 없으면
                    throw new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
                }
            }
            case USER -> {
                if(userMapper.findById(targetId).isEmpty()) { //신고대상(유저)이 없으면
                    throw new CustomException(ChatErrorCode.USER_NOT_FOUND);
                }
                if (targetId.equals(reporterId)) { //신고대상이 자신일 경우
                    throw new CustomException(ChatErrorCode.REPORT_SELF_NOT_ALLOWED);
                }
            }
        }
    }
}
