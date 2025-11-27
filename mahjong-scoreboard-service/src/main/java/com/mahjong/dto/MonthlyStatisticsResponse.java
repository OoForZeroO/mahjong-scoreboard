package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 月度统计响应模型
 */
public class MonthlyStatisticsResponse {
    
    @JsonProperty("wechatUserId")
    private String wechatUserId;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("avatar")
    private String avatar;
    
    @JsonProperty("yearMonth")
    private String yearMonth; // 格式：YYYY-MM
    
    @JsonProperty("totalScore")
    private Integer totalScore; // 总得分（倍率前）
    
    @JsonProperty("totalMultiplierScore")
    private Double totalMultiplierScore; // 总倍率分（倍率后）
    
    @JsonProperty("totalMatches")
    private Integer totalMatches; // 总对局数
    
    @JsonProperty("winMatches")
    private Integer winMatches; // 胜场数
    
    @JsonProperty("loseMatches")
    private Integer loseMatches; // 负场数
    
    @JsonProperty("winRate")
    private Double winRate; // 胜率
    
    @JsonProperty("winTotalScore")
    private Integer winTotalScore; // 胜场总分（倍率前）
    
    @JsonProperty("winTotalMultiplierScore")
    private Double winTotalMultiplierScore; // 胜场总倍率分（倍率后）
    
    @JsonProperty("loseTotalScore")
    private Integer loseTotalScore; // 负场总分（倍率前）
    
    @JsonProperty("loseTotalMultiplierScore")
    private Double loseTotalMultiplierScore; // 负场总倍率分（倍率后）
    
    @JsonProperty("highestScorePlayer")
    private PlayerScoreInfo highestScorePlayer; // 该时间段内胜分最多的玩家
    
    @JsonProperty("lowestScorePlayer")
    private PlayerScoreInfo lowestScorePlayer; // 该时间段内负分最多的玩家
    
    // 构造函数
    public MonthlyStatisticsResponse() {}
    
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
    
    public String getYearMonth() {
        return yearMonth;
    }
    
    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
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
    
    public Integer getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    public Integer getWinMatches() {
        return winMatches;
    }
    
    public void setWinMatches(Integer winMatches) {
        this.winMatches = winMatches;
    }
    
    public Integer getLoseMatches() {
        return loseMatches;
    }
    
    public void setLoseMatches(Integer loseMatches) {
        this.loseMatches = loseMatches;
    }
    
    public Double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }
    
    public Integer getWinTotalScore() {
        return winTotalScore;
    }
    
    public void setWinTotalScore(Integer winTotalScore) {
        this.winTotalScore = winTotalScore;
    }
    
    public Double getWinTotalMultiplierScore() {
        return winTotalMultiplierScore;
    }
    
    public void setWinTotalMultiplierScore(Double winTotalMultiplierScore) {
        this.winTotalMultiplierScore = winTotalMultiplierScore;
    }
    
    public Integer getLoseTotalScore() {
        return loseTotalScore;
    }
    
    public void setLoseTotalScore(Integer loseTotalScore) {
        this.loseTotalScore = loseTotalScore;
    }
    
    public Double getLoseTotalMultiplierScore() {
        return loseTotalMultiplierScore;
    }
    
    public void setLoseTotalMultiplierScore(Double loseTotalMultiplierScore) {
        this.loseTotalMultiplierScore = loseTotalMultiplierScore;
    }
    
    public PlayerScoreInfo getHighestScorePlayer() {
        return highestScorePlayer;
    }
    
    public void setHighestScorePlayer(PlayerScoreInfo highestScorePlayer) {
        this.highestScorePlayer = highestScorePlayer;
    }
    
    public PlayerScoreInfo getLowestScorePlayer() {
        return lowestScorePlayer;
    }
    
    public void setLowestScorePlayer(PlayerScoreInfo lowestScorePlayer) {
        this.lowestScorePlayer = lowestScorePlayer;
    }
}

