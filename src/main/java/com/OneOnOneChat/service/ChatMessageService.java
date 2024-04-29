package com.OneOnOneChat.service;

import com.OneOnOneChat.entity.ChatMessage;
import com.OneOnOneChat.repo.ChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ChatMessageService {

    ChatMessageRepository chatMessageRepository;
    ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage){
        if (!(chatMessage.getGroup().getId()>0) || Objects.equals(chatMessage.getGroup(), null)) {
            //this code block will run for personal-chat messages
            var chatId = chatRoomService.getChatRoomId(chatMessage.getSender().getFullName(), chatMessage.getRecipient().getFullName(), true, false)
                    .orElseThrow();
            chatMessage.setChatId(chatId);
            chatMessage.setTimestamp(new Date());
            chatMessageRepository.save(chatMessage);
        } else {
            //this code block will run for group-chat messages
            var chatId = chatRoomService.getChatRoomId(chatMessage.getSender().getFullName(), chatMessage.getGroup().getGroupName(), true, true)
                    .orElseThrow();
            chatMessage.setChatId(chatId);
            chatMessage.setTimestamp(new Date());
            chatMessageRepository.save(chatMessage);
        }
        return chatMessage;
    }
//
//    public List<ChatMessage> findChatMessages(String senderId, String recipientId){
//        var chatId = chatRoomService.getChatRoomId(senderId,recipientId,false);
//        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
//
//
//    }
}
