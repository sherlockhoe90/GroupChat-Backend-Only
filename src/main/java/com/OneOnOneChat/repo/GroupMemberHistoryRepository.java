package com.OneOnOneChat.repo;

import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.GroupMemberHistory;
import com.OneOnOneChat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupMemberHistoryRepository extends JpaRepository<GroupMemberHistory, Long> {

    @Query("SELECT gmh.user FROM GroupMemberHistory gmh WHERE gmh.group = :group AND gmh.leftAt > :cutoffTime")
    List<User> findUsersByGroupAndLeftAtBefore(GroupChatRoomEntity group, LocalDateTime cutoffTime);

    boolean existsByGroupAndUser(GroupChatRoomEntity group, User user);

    @Query("SELECT gmh.id FROM GroupMemberHistory gmh WHERE gmh.group.id = :groupId AND gmh.user.id = :userId")
    Long findIdByGroupAndUser(Long groupId, Long userId);
}
