package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface chatMessageRepository extends MongoRepository<ChatMessage,String> {

    List <ChatMessage> findByChatId(String s);
}
