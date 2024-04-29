package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,String> {

    Optional<ChatRoom> findBySenderIdAndRecipientId(String senderId, String recipientId);
    Optional<ChatRoom> findBySenderIdAndAndGroupId(String senderId, String groupId);
}

