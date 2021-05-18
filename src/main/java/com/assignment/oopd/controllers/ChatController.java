package com.assignment.oopd.controllers;

import com.assignment.oopd.models.ChatMessage;
import com.assignment.oopd.services.ChatService;
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

    @Autowired
    private ChatService chatService;

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
        messagingTemplate.convertAndSend("/topic/" + chatService.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor)
            throws NumberFormatException {

        simpMessageHeaderAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        simpMessageHeaderAccessor.getSessionAttributes().put("roomId", chatMessage.getRoomId());
        chatService.setCapacity(Integer.parseInt(chatMessage.getRoomCapacity()));
        chatService.setRoomId(chatMessage.getRoomId());

        if (!chatService.getRoomIds().contains(chatService.getRoomId())) {
            chatService.getRoomIds().add(chatService.getRoomId());
            Integer[] room_cap = new Integer[] { 0, chatService.getCapacity() };
            chatService.getRoomCapacity().putIfAbsent(chatService.getRoomId(), room_cap);
        }

        Integer[] b = chatService.getRoomCapacity().get(chatService.getRoomId());
        if (b[1] == b[0]) {

        } else if (b[1] > b[0]) {
            b[0]++;
        }

        messagingTemplate.convertAndSend("/topic/" + chatService.getRoomId(), chatMessage);
    }

    @GetMapping("/roomid-exists")
    public boolean roomIdAlreadyExists(@RequestParam(value = "room-id") String roomId) {
        if (chatService.getRoomIds().contains(roomId)) {
            return true;
        }
        return false;
    }

    @GetMapping("/roomid-capacity-check")
    public boolean roomIsNotFull(@RequestParam(value = "room-id") String roomId) {
        Integer[] b = chatService.getRoomCapacity().get(roomId);
        if (b[0] != b[1]) {
            return true;
        } else {
            return false;
        }
    }

    @GetMapping("/roomid-capacity-decrease")
    public void userLeft(@RequestParam(value = "room-id") String roomId) {
        Integer[] b = chatService.getRoomCapacity().get(roomId);
        if (b[0] > 1) {
            if (b[0] == 1) {
                b[0] = 0;
                chatService.getRoomCapacity().remove(roomId);
                chatService.getRoomIds().remove(roomId);
            } else {
                b[0] = b[0] - 1;
            }
        }
    }

}
