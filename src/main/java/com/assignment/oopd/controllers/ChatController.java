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
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatController {

    private ArrayList<String> roomIds = new ArrayList<String>();
    private String roomId;
    private Map<String, Integer[]> roomCapacity = new HashMap<String, Integer[]>();
    private Integer capacity;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{this.roomId}")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String session_username = simpMessageHeaderAccessor.getSessionAttributes().get("username").toString();

        if (!session_username.equals(chatMessage.getSender())) {
            chatMessage.setContent("Unauthorised User");
            messagingTemplate.convertAndSend("/topic/" + session_username, chatMessage);
            return;
        }
        messagingTemplate.convertAndSend("/topic/" + this.roomId, chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor)
            throws NumberFormatException {
        simpMessageHeaderAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        simpMessageHeaderAccessor.getSessionAttributes().put("roomId", chatMessage.getRoomId());
        this.capacity = Integer.parseInt(chatMessage.getRoomCapacity());
        this.roomId = chatMessage.getRoomId();
        if (!roomIds.contains(roomId)) {
            this.roomIds.add(roomId);
            Integer[] room_cap = new Integer[] { 0, capacity };
            this.roomCapacity.putIfAbsent(roomId, room_cap);
        }

        Integer[] b = roomCapacity.get(roomId);
        if (b[1] == b[0]) {

        } else if (b[1] > b[0]) {
            b[0]++;
        }

        messagingTemplate.convertAndSend("/topic/" + this.roomId, chatMessage);
    }

    @GetMapping("/roomid-exists")
    public boolean roomIdAlreadyExists(@RequestParam(value = "room-id") String roomId) {
        if (this.roomIds.contains(roomId)) {
            return true;
        }
        return false;
    }

    @GetMapping("/roomid-capacity-check")
    public boolean roomIsNotFull(@RequestParam(value = "room-id") String roomId) {
        Integer[] b = roomCapacity.get(roomId);
        if (b[0] != b[1]) {
            return true;
        } else {
            return false;
        }
    }

    @GetMapping("/roomid-capacity-decrease")
    public void userLeft(@RequestParam(value = "room-id") String roomId) {
        Integer[] b = roomCapacity.get(roomId);
        if (b[0] > 1) {
            if (b[0] == 1) {
                b[0] = 0;
                roomCapacity.remove(roomId);
                roomIds.remove(roomId);
            } else {
                b[0] = b[0] - 1;
            }
        }
    }

}
