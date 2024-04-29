package com.OneOnOneChat.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatRoomEntityRepository extends JpaRepository<GroupChatRoomEntity, Long> {

    Optional<User> findByMembersAndId(User member, Long groupId);


}