package com.OneOnOneChat.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

//@Document
@Data
@Builder
public class GroupChatMappingEntity {

    GroupChatRoomEntity groupId;

    User userEntityId;
}
