package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatRoomEntityRepository extends JpaRepository<GroupChatRoomEntity, Long> {

    Optional<User> findByMembersAndId(User member, Long groupId);


}