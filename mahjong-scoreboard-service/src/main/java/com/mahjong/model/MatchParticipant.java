package com.mahjong.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "match_participants")
public class MatchParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private WechatUser user;

    @Column(name = "wechat_user_id", length = 100)
    @JsonProperty("wechatUserId")
    private String wechatUserId;

    @Column(name = "nickname", nullable = false, length = 100)
    @JsonProperty("userName")
    private String userName;

    @Column(name = "avatar", length = 500)
    @JsonProperty("avatar")
    private String avatar;

    // 新的API字段映射 - 用于接收新的API格式
    @Transient
    @JsonProperty("nickName")
    private String nickName;

    @Transient
    @JsonProperty("avatarUrl")
    private String avatarUrl;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "is_quit", nullable = false)
    private Boolean isQuit = false;

    @Column(name = "quit_time")
    private Long quitTime;

    @Transient
    @JsonProperty("roundScore")
    private RoundScoreData roundScore;

    @Column(updatable = false)
    private Long createTime;

    @Column
    private Long updateTime;

    // 内部类：轮次计分数据传输对象
    public static class RoundScoreData {
        @JsonProperty("roundNumber")
        private Integer roundNumber;
        
        @JsonProperty("score")
        private Integer score;
        
        @JsonProperty("roundTime")
        private Long roundTime;

        // 构造函数
        public RoundScoreData() {
            this.roundNumber = 1; // 默认为第1轮
        }

        public RoundScoreData(Integer score) {
            this.roundNumber = 1; // 默认为第1轮
            this.score = score;
            this.roundTime = System.currentTimeMillis();
        }

        public RoundScoreData(Integer roundNumber, Integer score) {
            this.roundNumber = roundNumber != null ? roundNumber : 1; // 如果为null则默认为1
            this.score = score;
            this.roundTime = System.currentTimeMillis();
        }

        // Getter和Setter
        public Integer getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(Integer roundNumber) {
            this.roundNumber = roundNumber;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Long getRoundTime() {
            return roundTime;
        }

        public void setRoundTime(Long roundTime) {
            this.roundTime = roundTime;
        }
    }

    @PrePersist
    public void prePersist() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = System.currentTimeMillis();
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public WechatUser getUser() {
        return user;
    }

    public void setUser(WechatUser user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWechatUserId() {
        return wechatUserId;
    }

    public void setWechatUserId(String wechatUserId) {
        this.wechatUserId = wechatUserId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
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

    public RoundScoreData getRoundScore() {
        return roundScore;
    }

    public void setRoundScore(RoundScoreData roundScore) {
        this.roundScore = roundScore;
    }

    public Boolean getIsQuit() {
        return isQuit;
    }

    public void setIsQuit(Boolean isQuit) {
        this.isQuit = isQuit;
    }

    public Long getQuitTime() {
        return quitTime;
    }

    public void setQuitTime(Long quitTime) {
        this.quitTime = quitTime;
    }
}