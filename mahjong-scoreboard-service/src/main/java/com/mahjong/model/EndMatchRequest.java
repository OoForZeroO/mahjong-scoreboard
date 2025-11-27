package com.mahjong.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EndMatchRequest {
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("multiplier")
    private Double multiplier;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
