package com.OneOnOneChat.mapper;


import com.OneOnOneChat.dto.GroupAlterDTO;
import com.OneOnOneChat.dto.GroupDTO;
import com.OneOnOneChat.entity.GroupChatRoomEntity;
import com.OneOnOneChat.entity.User;

import java.util.stream.Collectors;

public class GroupMapper {
    public static GroupDTO groupEntityToGroupDTO(GroupChatRoomEntity entity) {
        return GroupDTO.builder()
                .groupName(entity.getGroupName())
                .groupDescription(entity.getGroupDescription())
                .memberIdList(entity.getMembers().stream().map(User::getId).collect(Collectors.toList()))
                .adminIdList(entity.getAdmins().stream().map(User::getId).collect(Collectors.toList()))
                .build();
    }
}
