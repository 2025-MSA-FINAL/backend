package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.dto.chat.response.ChatUserProfileResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatUserMapper {
    ChatUserProfileResponse getChatUserProfile(Long userId);
}
