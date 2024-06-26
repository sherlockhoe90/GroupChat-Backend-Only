package com.OneOnOneChat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is irrelevant to this project
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String content;

    private String senderId;
    private String senderFullName;

    private String groupId;
    private String groupName;
}
