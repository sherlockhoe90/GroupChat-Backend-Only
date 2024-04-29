package com.OneOnOneChat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupAlterDTO extends GroupDTO {

    String actingAdminId;
    List<String> newAdminIds;

}