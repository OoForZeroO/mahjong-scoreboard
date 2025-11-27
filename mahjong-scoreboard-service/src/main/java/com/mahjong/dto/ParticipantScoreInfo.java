package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 参与者得分信息DTO
 * 用于序列化到MatchResult的total_scores字段
 */
public class ParticipantScoreInfo {
    
    @JsonProperty("participantId")
    private Long participantId;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("avatar")
    private String avatar;
    
    @JsonProperty("totalScore")
    private Integer totalScore;
    
    @JsonProperty("finalScore")
    private Integer finalScore; // 倍率后的最终得分
    
    @JsonProperty("isWinner")
    private Boolean isWinner;
    
    @JsonProperty("wechatUserId")
    private String wechatUserId;
    
    // 构造函数
    public ParticipantScoreInfo() {}
    
    public ParticipantScoreInfo(Long participantId, String nickname, 
                               String avatar, Integer totalScore, Integer finalScore, 
                               Boolean isWinner, String wechatUserId) {
        this.participantId = participantId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.totalScore = totalScore;
        this.finalScore = finalScore;
        this.isWinner = isWinner;
        this.wechatUserId = wechatUserId;
    }
    
    // Getter和Setter方法
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
    
    public Integer getFinalScore() {
        return finalScore;
    }
    
    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }
    
    public Boolean getIsWinner() {
        return isWinner;
    }
    
    public void setIsWinner(Boolean isWinner) {
        this.isWinner = isWinner;
    }
    
    public String getWechatUserId() {
        return wechatUserId;
    }
    
    public void setWechatUserId(String wechatUserId) {
        this.wechatUserId = wechatUserId;
    }
}
