package com.OneOnOneChat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Document
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String chatId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupChatRoomEntity group;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    //private String senderId;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    //private String recipientId;

    private String content;
    private Date timestamp;

    public GroupChatRoomEntity getGroup() {
        return group;
    }
}
