package com.OneOnOneChat.mapper;

import com.OneOnOneChat.dto.ChatMessageDTO;
import com.OneOnOneChat.entity.ChatMessage;
import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.User;

public class ChatMessageMapper {

    public static ChatMessage dtoToEntity(ChatMessageDTO message) {
        return ChatMessage.builder()
                .content(message.getContent())
                .sender(User.builder().id(Long.valueOf(message.getSenderId())).fullName(message.getSenderFullName()).build())
                .group(GroupChatRoomEntity.builder().id(Long.valueOf(message.getGroupId())).groupChatId(message.getGroupId()).groupName(message.getGroupName()).build())
                .build();
    }
}