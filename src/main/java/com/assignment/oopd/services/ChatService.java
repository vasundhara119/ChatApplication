package com.assignment.oopd.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    private ArrayList<String> roomIds = new ArrayList<String>();
    private String roomId;
    private Map<String, Integer[]> roomCapacity = new HashMap<String, Integer[]>();
    private Integer capacity;

    public ArrayList<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(ArrayList<String> roomIds) {
        this.roomIds = roomIds;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Map<String, Integer[]> getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(Map<String, Integer[]> roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
