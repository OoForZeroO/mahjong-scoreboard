package com.mahjong.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MatchStatusQueryResponse {
    
    // 对局基本信息
    @JsonProperty("matchId")
    private Long matchId;
    
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("startTime")
    private Long startTime;
    
    @JsonProperty("endTime")
    private Long endTime;
    
    @JsonProperty("status")
    private Integer status; // 0:进行中, 1:已完成
    
    @JsonProperty("totalRounds")
    private Integer totalRounds;
    
    @JsonProperty("settlementMultiplier")
    private Double settlementMultiplier;
    
    @JsonProperty("createTime")
    private Long createTime;
    
    @JsonProperty("updateTime")
    private Long updateTime;
    
    // 参与者信息
    @JsonProperty("participants")
    private List<ParticipantSummary> participants;
    
    // 参与者摘要信息
    public static class ParticipantSummary {
        @JsonProperty("participantId")
        private Long participantId;
        
        @JsonProperty("nickName")
        private String nickName;
        
        @JsonProperty("avatar")
        private String avatar;
        
        @JsonProperty("totalScore")
        private Integer totalScore;
        
        @JsonProperty("wechatUserId")
        private String wechatUserId;
        
        @JsonProperty("isVisitor")
        private Boolean isVisitor;
        
        // Getter and Setter methods
        public Long getParticipantId() {
            return participantId;
        }
        
        public void setParticipantId(Long participantId) {
            this.participantId = participantId;
        }
        
        public String getNickName() {
            return nickName;
        }
        
        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        
        public String getAvatar() {
            return avatar;
        }
        
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
        
        public Integer getTotalScore() {
            return totalScore;
        }
        
        public void setTotalScore(Integer totalScore) {
            this.totalScore = totalScore;
        }
        
        public String getWechatUserId() {
            return wechatUserId;
        }
        
        public void setWechatUserId(String wechatUserId) {
            this.wechatUserId = wechatUserId;
        }
        
        public Boolean getIsVisitor() {
            return isVisitor;
        }
        
        public void setIsVisitor(Boolean isVisitor) {
            this.isVisitor = isVisitor;
        }
    }
    
    // Getter and Setter methods
    public Long getMatchId() {
        return matchId;
    }
    
    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    public Long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
    
    public Double getSettlementMultiplier() {
        return settlementMultiplier;
    }
    
    public void setSettlementMultiplier(Double settlementMultiplier) {
        this.settlementMultiplier = settlementMultiplier;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
    
    public List<ParticipantSummary> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantSummary> participants) {
        this.participants = participants;
    }
}
