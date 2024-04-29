package com.OneOnOneChat.service;

import com.OneOnOneChat.entity.ChatRoom;
import com.OneOnOneChat.entity.User;
import com.OneOnOneChat.repo.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ChatRoomService {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    public Optional<String> getChatRoomId(String sender, String recipient, boolean createNewRoomIfNotExists, boolean isGroup) {
        try {
            //if simple personal chat
            if (!isGroup) {
                return chatRoomRepository
                        .findBySenderIdAndRecipientId(sender, recipient)
                        .map(ChatRoom::getChatId)
                        .or(() -> {
                            if (createNewRoomIfNotExists) {
                                String chatId = null;
                                try {
                                    chatId = createChatId(sender, recipient, false);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                return Optional.of(chatId);
                            }
                            return Optional.empty();
                        });
            }
            //if group chat
            else {
                //TODO: add support for sending/forwarding to multiple groups
                return chatRoomRepository
                        .findBySenderIdAndAndGroupId(sender, recipient)
                        .map(ChatRoom::getChatId)
                        .or(() -> {
                            if (createNewRoomIfNotExists) {
                                String chatId = null;
                                try {
                                    chatId = createChatId(sender, recipient, true);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                return Optional.of(chatId);
                            }
                            return Optional.empty();
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private String createChatId(String senderId, String recipientId, boolean isGroup) throws Exception {

        var chatId = String.format("%s_%s", senderId, recipientId);
        chatId.replace(" ", "_");
        ChatRoom senderRecipient;
        ChatRoom recipientSender;

        if (!isGroup) {
            senderRecipient = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(String.valueOf(senderId))
                    .recipientId(String.valueOf(recipientId))
                    .build();

            recipientSender = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(String.valueOf(recipientId))
                    .recipientId(String.valueOf(senderId))
                    .build();
        } else {
            senderRecipient = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(String.valueOf(senderId))
                    .groupId(String.valueOf(recipientId))
                    .build();

            recipientSender = ChatRoom.builder()
                    .chatId(chatId)
                    .senderId(String.valueOf(recipientId))
                    .recipientId(String.valueOf(senderId))
                    .build();
        }
        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }


}
