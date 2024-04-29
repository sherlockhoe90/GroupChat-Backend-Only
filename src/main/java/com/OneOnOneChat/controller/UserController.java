package com.OneOnOneChat.controller;

import com.OneOnOneChat.entity.User;
import com.OneOnOneChat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/")
//    @GetMapping("/mainPage")
    public String mainPage(){
        return "index";
    }

    @MessageMapping("/user.addUser")
    @SendTo("/user/public")
    @GetMapping("/user/addUser")
    public ResponseEntity<User> addUser(@Payload @RequestBody User user){
        service.saveUser(user);
        return ResponseEntity.ok(user);
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/public")
    public User disconnect(@Payload User user){
        service.disconnect(user);
        return user;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUser(){
        return ResponseEntity.ok(service.findConnectedUser());
    }




}
