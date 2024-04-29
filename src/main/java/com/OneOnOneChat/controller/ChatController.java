package com.OneOnOneChat.controller;

import com.OneOnOneChat.entity.ChatMessage;
import com.OneOnOneChat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is irrelevant to this project
 */
@Controller
public class ChatController {
    @Autowired
    SimpMessagingTemplate messagingTemplate;
    @Autowired
    ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage){
//        ChatMessage savedMsg = chatMessageService.(chatMessage);
//        messagingTemplate.convertAndSendToUser(chatMessage.getRecipient(),"/queue/messages", ChatNotification.builder().id(String.valueOf(savedMsg.getId())).senderId(savedMsg.getSender()).recipientId(savedMsg.getRecipient()).content(savedMsg.getContent()).build());
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable("senderId") String senderId,@PathVariable("recipientId") String recipientId){
//        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId,recipientId));
        return ResponseEntity.ok(new ArrayList<>());
    }

}
