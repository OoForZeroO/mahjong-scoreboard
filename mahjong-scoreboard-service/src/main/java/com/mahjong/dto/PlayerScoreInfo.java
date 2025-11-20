package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 玩家得分信息DTO
 * 用于月度统计中的最高分/最低分玩家信息
 */
public class PlayerScoreInfo {
    
    @JsonProperty("wechatUserId")
    private String wechatUserId;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("avatar")
    private String avatar;
    
    @JsonProperty("totalScore")
    private Integer totalScore;
    
    @JsonProperty("totalMultiplierScore")
    private Double totalMultiplierScore;
    
    // 构造函数
    public PlayerScoreInfo() {}
    
    public PlayerScoreInfo(String wechatUserId, String nickname, String avatar, 
                          Integer totalScore, Double totalMultiplierScore) {
        this.wechatUserId = wechatUserId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.totalScore = totalScore;
        this.totalMultiplierScore = totalMultiplierScore;
    }
    
    // Getter和Setter
    public String getWechatUserId() {
        return wechatUserId;
    }
    
    public void setWechatUserId(String wechatUserId) {
        this.wechatUserId = wechatUserId;
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
    
    public Double getTotalMultiplierScore() {
        return totalMultiplierScore;
    }
    
    public void setTotalMultiplierScore(Double totalMultiplierScore) {
        this.totalMultiplierScore = totalMultiplierScore;
    }
}

