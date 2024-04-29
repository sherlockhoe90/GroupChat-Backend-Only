package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.GroupChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoomEntity, Long> {

    Short countById(Long id);

    @Query("SELECT COUNT(DISTINCT m) FROM GroupChatRoomEntity g JOIN g.members m WHERE g.id = :groupId")
    Short memberCountByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM GroupChatRoomEntity g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId")
    boolean doesMemberExist(Long groupId, Long userId);

    @Query("SELECT CASE WHEN (COUNT(a) > 0) THEN TRUE ELSE FALSE END FROM GroupChatRoomEntity g JOIN g.admins a WHERE g.id = :groupId AND a.id = :userId")
    boolean doesAdminExist(Long groupId, Long userId);
}