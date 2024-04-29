package com.OneOnOneChat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDTO {
    private String groupName;
    private String groupDescription;
    private List<Long> memberIdList;
    private List<Long> adminIdList;
}
