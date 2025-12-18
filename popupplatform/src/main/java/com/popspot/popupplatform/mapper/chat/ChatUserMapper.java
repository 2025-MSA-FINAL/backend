package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.dto.chat.response.ChatUserProfileResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatUserMapper {
    //유저프로필
    ChatUserProfileResponse getChatUserProfile(Long userId);
}
