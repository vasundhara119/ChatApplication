package com.assignment.oopd.controllers;

import com.assignment.oopd.models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class ChatController {

    private ArrayList<String> roomIds = new ArrayList<String>();
    private String roomId;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{this.roomId}")
    @SendTo("/topic/{this.roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        simpMessageHeaderAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        this.roomId = chatMessage.getRoomId();
        if(!roomIds.contains(roomId)) {
            this.roomIds.add(roomId);
        }
        messagingTemplate.convertAndSend("/topic/"+this.roomId, chatMessage);
    }

    @GetMapping("/roomid-exists")
    public boolean roomIdAlreadyExists(@RequestParam(value = "room-id") String roomId) {
        if(this.roomIds.contains(roomId)) {
            return true;
        }
        return false;
    }
}
