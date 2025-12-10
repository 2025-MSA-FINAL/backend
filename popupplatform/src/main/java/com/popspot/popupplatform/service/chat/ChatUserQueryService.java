package com.popspot.popupplatform.service.chat;


import com.popspot.popupplatform.dto.chat.response.ChatUserProfileResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatUserQueryService {

    private final ChatUserMapper chatUserMapper;

    public ChatUserProfileResponse getChatUserProfile(Long userId) {
        ChatUserProfileResponse profile = chatUserMapper.getChatUserProfile(userId);
        if (profile == null) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
        return profile;
    }

}
