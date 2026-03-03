package com.mahjong.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 对局详情中的参与者详情（与 MatchDetailResponse 配套使用）
 */
public class ParticipantDetail {
    @JsonProperty("participantId")
    private Long participantId;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("avatar")
    private String avatar;
    
    @JsonProperty("totalScore")
    private Integer totalScore;
    
    @JsonProperty("isVisitor")
    private Boolean isVisitor;
    
    @JsonProperty("isQuit")
    private Boolean isQuit;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("wechatUserId")
    private String wechatUserId;
    
    public ParticipantDetail() {}
    
    public ParticipantDetail(Long participantId, String nickname, String avatar, Integer totalScore,
                             Boolean isVisitor, Boolean isQuit, Long userId) {
        this.participantId = participantId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.totalScore = totalScore;
        this.isVisitor = isVisitor;
        this.isQuit = isQuit;
        this.userId = userId;
    }
    
    public Long getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
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
    
    public Boolean getIsVisitor() {
        return isVisitor;
    }
    
    public void setIsVisitor(Boolean isVisitor) {
        this.isVisitor = isVisitor;
    }
    
    public Boolean getIsQuit() {
        return isQuit;
    }
    
    public void setIsQuit(Boolean isQuit) {
        this.isQuit = isQuit;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getWechatUserId() {
        return wechatUserId;
    }
    
    public void setWechatUserId(String wechatUserId) {
        this.wechatUserId = wechatUserId;
    }
}
