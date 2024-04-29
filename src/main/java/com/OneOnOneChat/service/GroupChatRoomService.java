package com.OneOnOneChat.service;

import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.GroupMemberHistory;
import com.OneOnOneChat.entity.User;
import com.OneOnOneChat.repo.GroupChatRoomRepository;
import com.OneOnOneChat.repo.GroupMemberHistoryRepository;
import com.OneOnOneChat.repo.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatRoomService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final UserRepository userRepository;
    private final GroupMemberHistoryRepository groupMemberHistoryRepository;

    public GroupChatRoomEntity createGroup(String groupName, String groupDescription, List<Long> memberIds, Long adminId) {
        try {
            GroupChatRoomEntity groupChat = new GroupChatRoomEntity();
            groupChat.setGroupName(groupName);
            groupChat.setGroupDescription(groupDescription);

            //saving every member in both the tables, the general and the mapping one
            List<User> members = userRepository.findAllById(memberIds);
            groupChat.setMembers(members);

            //saving every admin in both the tables for mapping
            Optional<User> adminUser = userRepository.findById(adminId);
            adminUser.ifPresent(user -> {
                groupChat.getAdmins().add(user);
                user.getGroups().add(groupChat);
                userRepository.save(user);
            });

            log.info("Group with admin user id: " + adminId + " and name: " + groupName + " created successfully.");
            return groupChatRoomRepository.save(groupChat);

        } catch (Exception e) {

            e.printStackTrace();
            log.info("Could not create the group.");
            return new GroupChatRoomEntity();

        }
    }

    public void deleteGroup(Long groupId, Long actingUserId) throws InstanceNotFoundException, AuthenticationException {
        try {
            if (!groupChatRoomRepository.existsById(groupId)) throw new InstanceNotFoundException();
            if (!groupChatRoomRepository.doesAdminExist(groupId, actingUserId))
                throw new AuthenticationException("The user is not an Admin, and is unuthorized.");
            groupChatRoomRepository.deleteById(groupId);
            log.info("Group with id : " + groupId + " deleted successfully.");
        } catch (InstanceNotFoundException | AuthenticationException e) {
            e.printStackTrace();
            log.debug("The group does not exist.");
            throw e;
        }
    }

    //TODO: take in a list of memberIds to remove multiple users, instead ofa  single memberId
    public void removeMemberFromGroup(Long groupId, Long memberId, Long actingUserId) throws InstanceNotFoundException, IllegalArgumentException, AuthenticationException {
        try {
            Optional<GroupChatRoomEntity> optionalGroupChat = groupChatRoomRepository.findById(groupId);
            User member = userRepository.findById(memberId).orElseThrow();
            if (!optionalGroupChat.isPresent())
                throw new InstanceNotFoundException("The group with id " + groupId + " was not found.");
            if (!groupChatRoomRepository.doesAdminExist(Long.valueOf(groupId), Long.valueOf(actingUserId)))
                throw new AuthenticationException("The user is not an Admin, and is unuthorized.");
            if (groupMemberHistoryRepository.existsByGroupAndUser(optionalGroupChat.get(), member)) {
                throw new IllegalArgumentException("The member IS NOT a part of the group.");
            }
            optionalGroupChat.ifPresent(groupChat -> {
                groupChat.getMembers().removeIf(user -> user.getId().equals(memberId));
                groupChatRoomRepository.save(groupChat);

                GroupMemberHistory historyEntry = new GroupMemberHistory();
                historyEntry.setUser(member);
                historyEntry.setGroup(groupChat);
                historyEntry.setLeftAt(LocalDateTime.now());
                groupMemberHistoryRepository.save(historyEntry);

                log.info("The user with id: " + memberId + " was removed from the group id: " + groupChat.getId() + " successfully.");
            });
        } catch (InstanceNotFoundException | AuthenticationException | IllegalArgumentException e) {
            e.printStackTrace();
            log.debug("Could not remove the member from the group. The user may not exist.");

            throw e;
        }
    }

    //TODO: add a method variable outside the try-catch and save the memberIds in it,
    // removing a member at the time it is found by id. If the member is still in the collection,
    // it means it was not found in the DB.
    public void addMembersToGroup(Long groupId, List<Long> memberIds, Long actingUserId) throws IllegalArgumentException, InstanceNotFoundException, AuthenticationException {
        try {
            if (!groupChatRoomRepository.existsById(groupId))
                throw new InstanceNotFoundException("The group with id " + groupId + " was not found.");
            if (!groupChatRoomRepository.doesAdminExist(groupId, actingUserId))
                throw new AuthenticationException("The user is not an Admin, and is unauthorized.");
            Optional<GroupChatRoomEntity> optionalGroupChat = groupChatRoomRepository.findById(groupId);
            //using this because none of the synchronization method i tried was able to avoid the ConcurrentModificationException
            List<Long> listOfExistingMembers = new ArrayList<>();
            for (short i = 0; i < memberIds.size(); i++) {
                if (groupChatRoomRepository.doesMemberExist(groupId, memberIds.get(i)))
                    listOfExistingMembers.add(memberIds.get(i));
            }
            memberIds.removeAll(listOfExistingMembers);
            listOfExistingMembers.clear();
            optionalGroupChat.ifPresent(groupChat -> {
                memberIds.stream()
                        .forEach(memberId -> {
                            if (!userRepository.existsById(memberId))
                                throw new IllegalArgumentException("A member with the memberId: " + memberId + " does not exist.");
                            User user = userRepository.findById(memberId).get();
                            if (groupMemberHistoryRepository.existsByGroupAndUser(optionalGroupChat.get(), user))
                                groupMemberHistoryRepository.deleteById(groupMemberHistoryRepository.findIdByGroupAndUser(groupId, memberId));
                        });
                List<User> newMembers = userRepository.findAllById(memberIds);
                groupChat.getMembers().addAll(newMembers);
                groupChatRoomRepository.save(groupChat);
                log.info("Users with the given id's were added to the group with id:" + groupId + " and name: " + groupChat.getGroupName() + " successfully.");
            });
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
            log.debug("Could not add the members to the group.");
            throw e;
        } catch (IllegalArgumentException | AuthenticationException e) {
            e.printStackTrace();
            throw e;
        }
    }

    //TODO: take in a list of memberIds to add multiple admins, instead of a single memberId
    public void addAdminToGroup(Long groupId, Long adminId, Long actingUserId) throws InstanceNotFoundException, EntityExistsException, AuthenticationException {
        try {
            Optional<GroupChatRoomEntity> optionalGroupChat = groupChatRoomRepository.findById(groupId);
            if (!optionalGroupChat.isPresent())
                throw new InstanceNotFoundException("The group with the id: " + groupId + " does not exist.");
            if (!groupChatRoomRepository.doesAdminExist(groupId, actingUserId))
                throw new AuthenticationException("The user id: " + actingUserId + " is not an Admin, and is unauthorized.");
            optionalGroupChat.ifPresent(groupChat -> {
                if (!userRepository.existsById(adminId))
                    throw new IllegalArgumentException("A user with the userId: " + adminId + " does not exist.");
                if (groupChatRoomRepository.doesAdminExist(groupId, adminId))
                    throw new IllegalArgumentException("An admin with the userId: " + adminId + " already exists in the group with groupId: " + groupId);
                Optional<User> adminUser = userRepository.findById(adminId);
                adminUser.ifPresent(user -> {
                    groupChat.getAdmins().add(user);
                    groupChatRoomRepository.save(groupChat);
                    log.info("The user with id: " + adminId + " was given admin privileges for the group id: " + groupChat.getId() + " successfully.");
                });
            });
        } catch (InstanceNotFoundException | AuthenticationException e) {
            e.printStackTrace();
            log.debug("Could not add the user as admin. The user may not exist.");
            throw e;
        } catch (EntityExistsException e) {
            e.printStackTrace();
            log.debug("An admin with the userId: " + adminId + " already exists in the group with groupId: " + groupId);
            throw e;
        }
    }

    public void removeAdminFromGroup(Long groupId, Long adminId, Long actingUserId) throws InstanceNotFoundException, IllegalArgumentException, AuthenticationException {
        try {
            Optional<GroupChatRoomEntity> optionalGroupChat = groupChatRoomRepository.findById(groupId);
            if (!optionalGroupChat.isPresent())
                throw new InstanceNotFoundException("The group with the id: " + groupId + " does not exist.");
            if (!groupChatRoomRepository.doesAdminExist(groupId, actingUserId))
                throw new AuthenticationException("The user id: " + actingUserId + " is not an Admin, and is unauthorized.");
            optionalGroupChat.ifPresent(groupChat -> {
                if (!userRepository.existsById(adminId))
                    throw new IllegalArgumentException("A user with the userId: " + adminId + " does not exist.");
                if (!groupChatRoomRepository.doesAdminExist(groupId, adminId))
                    throw new IllegalArgumentException("An admin with the userId: " + adminId + " DOES NOT EXIST in the group with groupId: " + groupId);
                groupChat.getAdmins().removeIf(user -> user.getId().equals(adminId));
                groupChatRoomRepository.save(groupChat);
                log.info("The admin with user id: " + adminId + " was removed as admin from the group id :" + groupChat.getId() + " successfully.");
            });
        } catch (InstanceNotFoundException | IllegalArgumentException | AuthenticationException e) {
            e.printStackTrace();
            log.debug("Could not remove user from admin role. The user may not exist.");
            throw e;
        }
    }

    public GroupChatRoomEntity updateGroup(Long groupId, String newGroupName, String newGroupDescription, Long actingUserId) throws IllegalArgumentException, AuthenticationException {
        try {
            if (Objects.isNull(newGroupDescription)) newGroupDescription = "";
            if (Objects.isNull(newGroupName)) newGroupName = "";
            GroupChatRoomEntity groupChat = groupChatRoomRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group with ID: " + groupId + " was not found"));
            if (!groupChatRoomRepository.doesAdminExist(groupId, actingUserId))
                throw new AuthenticationException("The user id: " + actingUserId + " is not an Admin, and is unauthorized.");
            if (!newGroupName.isEmpty()) groupChat.setGroupName(newGroupName);
            if (!newGroupDescription.isEmpty()) groupChat.setGroupDescription(newGroupDescription);
            return groupChatRoomRepository.save(groupChat);
    } catch(IllegalArgumentException | AuthenticationException e)    {
        e.printStackTrace();
        log.debug("Group not found with ID: " + groupId + " was not found.");
        throw e;
    }

}


    public List<GroupChatRoomEntity> findAll() {
        return groupChatRoomRepository.findAll();
    }

    public Optional<GroupChatRoomEntity> findById(Long groupId) throws EntityNotFoundException {
        if (!groupChatRoomRepository.existsById(groupId))
            throw new EntityNotFoundException("The group with id: " + groupId + "does not exist.");
        return groupChatRoomRepository.findById(groupId);
    }

    public Short memberCountByGroupId(Long groupId) throws IllegalArgumentException {
        if (groupId > Long.valueOf("1024"))
            throw new IllegalArgumentException("The member count cannot be greater than 1024.");

        if (groupChatRoomRepository.existsById(groupId))
            return groupChatRoomRepository.memberCountByGroupId(Long.valueOf(groupId));
        else throw new IllegalArgumentException("The group with id: " + groupId + " does not exist.");
    }

    public List<User> pastParticipantsByGroupIdAndTimePeriod(Long groupId, int minutes) throws IllegalArgumentException {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);

            GroupChatRoomEntity groupChat = groupChatRoomRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

            return groupMemberHistoryRepository.findUsersByGroupAndLeftAtBefore(groupChat, cutoffTime);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            log.debug("No group with the id: " + groupId + " was found.");
            throw e;
        }
    }


    //memberIds will only be given at the creation of group, not every time
//    public Optional<String> getGroupChatRoomId(String groupName, String groupDescription, String senderId, String memberIds, boolean createNewRoomIfNotExists) {
//
//        return groupChatRoomRepository
//                .findByAdminIdInAndGroupName(senderId, groupName)
//                .map(GroupChatRoomEntity::getId)
//                .or(() -> {
//                    if (createNewRoomIfNotExists) {
//                        var chatId = createGroupChatId(groupName, groupDescription, senderId, memberIds);
//                        return Optional.of(chatId);
//                    }
//                    return Optional.empty();
//                });
//    }
//
//    private String createGroupChatId(String senderId, String groupName, String groupDescription, String memberIds) {
//
//        memberIds.concat("," + senderId);
//        List<User> usersList = userRepository.findAllById(List.of(memberIds));
//
//        GroupChatRoomEntity group = GroupChatRoomEntity.builder()
//                .adminIdList(List.of(senderId))
//                .groupName(groupName)
//                .groupDescription(groupDescription)
//                .build();
//
//        return groupChatRoomRepository.save(group).getId();
//    }
//
//    public GroupChatRoomEntity createGroupChat(String groupName, String groupDescription, String senderId, String memberIds) {
//
//        memberIds.concat("," + senderId);
//        List<User> usersList = userRepository.findAllById(List.of(memberIds));
//
//        GroupChatRoomEntity group = GroupChatRoomEntity.builder()
//                .groupName(groupName)
//                .groupDescription(groupDescription)
//                .adminIdList(List.of(senderId))
//                .memberIdList(List.of(memberIds))
//                .build();
//
//        return groupChatRoomRepository.save(group);
//    }
//
//    public boolean addGroupAdmins(GroupAlterDTO dto) {
//        try {
//            GroupChatRoomEntity group = groupChatRoomRepository.findByAdminIdInAndGroupName(dto.getActingAdminId(), dto.getGroupName()).orElseThrow(ChangeSetPersister.NotFoundException::new);
//
//            List<User> adminIds = group.getAdmins();
//            for (String s : dto.getNewAdminIds()) {
//                adminIds.add(s);
//            }
//            group.setAdmins(adminIds);
//
//            groupChatRoomRepository.save(group);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean removeGroupAdmins(GroupAlterDTO dto) {
//        try {
//            GroupChatRoomEntity group = groupChatRoomRepository.findByAdminIdInAndGroupName(dto.getActingAdminId(), dto.getGroupName()).get();
//
//            List<User> adminIds = group.getAdmins();
//            adminIds.removeAll(dto.getNewAdminIds());
//            group.setAdmins(adminIds);
//
//            groupChatRoomRepository.save(group);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}