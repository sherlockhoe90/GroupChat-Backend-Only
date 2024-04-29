package com.OneOnOneChat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
//@Document
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    //@Id
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String nickName;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(mappedBy = "members")
    private List<GroupChatRoomEntity> groups;

    @OneToMany
    private List<ChatMessage> message;

}
