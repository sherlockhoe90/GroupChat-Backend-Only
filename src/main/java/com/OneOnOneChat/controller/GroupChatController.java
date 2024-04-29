package com.OneOnOneChat.controller;

import com.OneOnOneChat.dto.ChatMessageDTO;
import com.OneOnOneChat.dto.GroupDTO;
import com.OneOnOneChat.entity.ChatMessage;
import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.User;
import com.OneOnOneChat.mapper.ChatMessageMapper;
import com.OneOnOneChat.mapper.GroupMapper;
import com.OneOnOneChat.service.ChatMessageService;
import com.OneOnOneChat.service.GroupChatRoomService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.management.InstanceNotFoundException;
import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/groups")
public class GroupChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final GroupChatRoomService groupChatRoomService;


    /**
    * RequestMappings work with Postman as they are HTTP based requests
    * In order to test the @MessageMapping methods we'll have to use the e
    */

    @MessageMapping("/groupChat")
    @PostMapping("/groupChat")
//    public void processMessage(@Payload @RequestBody ChatMessage chatMessage){
    public void processMessage(@Payload @RequestBody ChatMessageDTO chatMessageDTO){
        ChatMessage chatMessage = ChatMessageMapper.dtoToEntity(chatMessageDTO);
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        //the message will be sent to "/topic/Example_Group/1/queue/messages"
//        messagingTemplate.convertAndSend("/topic/Example_Group/"+chatMessage.getGroup().getId().toString(), ChatNotification.builder().id(String.valueOf(savedMsg.getId())).senderId(savedMsg.getSender().toString()).recipientId(savedMsg.getRecipient().toString()).content(savedMsg.getContent()).build());
    }

    @PostMapping("/new")
    public ResponseEntity<GroupDTO> createGroup(@RequestBody GroupDTO request) {
        if (!request.getMemberIdList().containsAll(request.getAdminIdList()) || request.getGroupName().isEmpty() || request.getMemberIdList().isEmpty())
            return new ResponseEntity<>(new GroupDTO(), HttpStatus.valueOf(422));
        GroupChatRoomEntity groupChat = groupChatRoomService.createGroup(request.getGroupName(), request.getGroupDescription(),
                request.getMemberIdList(), request.getAdminIdList().get(0));
        return new ResponseEntity<>(GroupMapper.groupEntityToGroupDTO(groupChat), HttpStatus.CREATED);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId, @RequestBody Long actingUserId) {
        try {
            groupChatRoomService.deleteGroup(groupId, actingUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (InstanceNotFoundException | AuthenticationException e) {
            log.warn("Could not delete the group.");
            return new ResponseEntity<>("Could not delete the group. invalid ID", HttpStatus.valueOf(422));
        }
    }

    @PostMapping("/remove-member/{groupId}/{memberId}")
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long memberId, @RequestBody String actingUserId) {
        try {
            groupChatRoomService.removeMemberFromGroup(groupId, memberId, Long.valueOf(actingUserId));
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (InstanceNotFoundException | IllegalArgumentException e) {
            log.warn("Could not delete the member.");
            return new ResponseEntity<>("Could not remove the member. " + e.getMessage(), HttpStatus.valueOf(422));
        } catch (AuthenticationException e) {
            log.debug("The user with id "+actingUserId+" is not authorized and hence could not remove the member.");
            return new ResponseEntity<>("Could not remove the member. " + e.getMessage(), HttpStatus.valueOf(422));
        }
    }

    @PostMapping("/add-members/{groupId}/{actingUserId}")
    public ResponseEntity<String> addMembersToGroup(@PathVariable Long groupId, @RequestBody(required = true) List<Long> memberIds, @PathVariable Long actingUserId) {
        try {
            groupChatRoomService.addMembersToGroup(groupId, memberIds, actingUserId);
            return new ResponseEntity<>("Successfully added all the members.", HttpStatus.OK);
        } catch (InstanceNotFoundException | AuthenticationException e) {
            log.warn("Could not add the members: "+memberIds.toString()+" to the group with id: "+groupId);
            return new ResponseEntity<>("Could not add the members. " + e.getMessage(), HttpStatus.valueOf(422));
        }  catch (IllegalArgumentException e) {
            log.warn("A member with the memberId: "+e.getMessage()+" does not exist.");
            return new ResponseEntity<>("Could not add the members. " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add-admin/{groupId}/{memberId}")
    public ResponseEntity<String> addAdminToGroup(@PathVariable Long groupId, @PathVariable Long memberId, @RequestBody Long actingUserId) {
        try {
            groupChatRoomService.addAdminToGroup(groupId, memberId, actingUserId);
            return new ResponseEntity<>("Successfully added the user as admin.", HttpStatus.OK);
        }catch (InstanceNotFoundException | AuthenticationException e) {
            log.warn("Could not add the member with id : "+memberId+" as admin of group with id: "+groupId+".");
            return new ResponseEntity<>("Could not add the user as admin. " + e.getMessage(), HttpStatus.valueOf(422));
        } catch (IllegalArgumentException | EntityExistsException e) {
            log.warn("A member with the memberId: "+e.getMessage()+" does not exist.");
            return new ResponseEntity<>("Could not add the admin. " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/remove-admin/{groupId}/{memberId}")
    public ResponseEntity<String> removeAdminFromGroup(@PathVariable Long groupId, @PathVariable Long memberId, @RequestBody Long actingUserId) {
        try {
            groupChatRoomService.removeAdminFromGroup(groupId, memberId, actingUserId);
            return new ResponseEntity<>("Successfully removed the user from admin.", HttpStatus.OK);
        }catch (InstanceNotFoundException | AuthenticationException e) {
            log.warn("Could not remove the user with id : "+memberId+" as admin of group with id: "+groupId+".");
            return new ResponseEntity<>("Could not remove the user from admin. " + e.getMessage(), HttpStatus.valueOf(422));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Could not remove the admin. " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getAll")
    public List<GroupChatRoomEntity> findAllGroups() {
        return groupChatRoomService.findAll();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> findById(@PathVariable String groupId) {
         try {
             Optional<GroupChatRoomEntity> group = groupChatRoomService.findById(Long.valueOf(groupId));
             return new ResponseEntity<>(GroupMapper.groupEntityToGroupDTO(group.get()), HttpStatus.OK);
         } catch (EntityNotFoundException e) {
             log.debug("Could not find the said group.");
             return new ResponseEntity<>(new GroupDTO(), HttpStatus.BAD_REQUEST);
         }
    }

    @GetMapping("/member-count/{groupId}")
    public ResponseEntity<Short> findCountByGroupId(@PathVariable String groupId) {
        try {
            Short count = groupChatRoomService.memberCountByGroupId(Long.valueOf(groupId));
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            log.debug(e.getMessage());
            return new ResponseEntity<>(Short.valueOf("-1"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable Long groupId,
                                                @RequestParam(required = false) String newGroupName,
                                                @RequestParam(required = false) String newGroupDescription,
                                                @RequestBody(required = true) Long actingUserId) {
        try {
            if ((Objects.isNull(newGroupName) && Objects.isNull(newGroupDescription))) throw new IllegalArgumentException();
            GroupChatRoomEntity updatedGroup = groupChatRoomService.updateGroup(groupId, newGroupName, newGroupDescription, actingUserId);
            return new ResponseEntity<>(GroupMapper.groupEntityToGroupDTO(updatedGroup), HttpStatus.OK);
        } catch (IllegalArgumentException | AuthenticationException e) {
            log.debug(e.getMessage());
            return new ResponseEntity<>(new GroupDTO(), HttpStatus.BAD_REQUEST);
        }
    }

    //users have left within the past {minutes} minutes
    @GetMapping("/past-participants/{groupId}")
    public ResponseEntity<List<User>> findPastParticipants(@PathVariable Long groupId, @RequestParam int minutes) {
        try {
            List<User> pastParticipants = groupChatRoomService.pastParticipantsByGroupIdAndTimePeriod(groupId, minutes);
            return new ResponseEntity<>(pastParticipants, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }
    }


}