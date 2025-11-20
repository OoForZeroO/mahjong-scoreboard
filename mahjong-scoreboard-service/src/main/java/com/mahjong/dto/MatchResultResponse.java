package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 对局结果响应DTO
 * 包含MatchResult表的所有数据
 */
public class MatchResultResponse {
    
    @JsonProperty("matchId")
    private Long matchId;
    
    @JsonProperty("winnerId")
    private Long winnerId;
    
    @JsonProperty("winnerNickname")
    private String winnerNickname;
    
    @JsonProperty("winnerAvatar")
    private String winnerAvatar;
    
    @JsonProperty("highestScore")
    private Integer highestScore;
    
    @JsonProperty("lowestScore")
    private Integer lowestScore;
    
    @JsonProperty("totalDuration")
    private Long totalDuration;
    
    @JsonProperty("totalScores")
    private String totalScores; // JSON字符串
    
    @JsonProperty("participantScores")
    private List<ParticipantScoreInfo> participantScores; // 解析后的JSON数据
    
    @JsonProperty("completionTime")
    private Long completionTime;
    
    @JsonProperty("createTime")
    private Long createTime;
    
    @JsonProperty("updateTime")
    private Long updateTime;
    
    // 构造函数
    public MatchResultResponse() {}
    
    public MatchResultResponse(Long matchId, Long winnerId, String winnerNickname, String winnerAvatar,
                             Integer highestScore, Integer lowestScore, Long totalDuration,
                             String totalScores, List<ParticipantScoreInfo> participantScores,
                             Long completionTime, Long createTime, Long updateTime) {
        this.matchId = matchId;
        this.winnerId = winnerId;
        this.winnerNickname = winnerNickname;
        this.winnerAvatar = winnerAvatar;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.totalDuration = totalDuration;
        this.totalScores = totalScores;
        this.participantScores = participantScores;
        this.completionTime = completionTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
    
    // Getter和Setter方法
    public Long getMatchId() {
        return matchId;
    }
    
    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
    
    public Long getWinnerId() {
        return winnerId;
    }
    
    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }
    
    public String getWinnerNickname() {
        return winnerNickname;
    }
    
    public void setWinnerNickname(String winnerNickname) {
        this.winnerNickname = winnerNickname;
    }
    
    public String getWinnerAvatar() {
        return winnerAvatar;
    }
    
    public void setWinnerAvatar(String winnerAvatar) {
        this.winnerAvatar = winnerAvatar;
    }
    
    public Integer getHighestScore() {
        return highestScore;
    }
    
    public void setHighestScore(Integer highestScore) {
        this.highestScore = highestScore;
    }
    
    public Integer getLowestScore() {
        return lowestScore;
    }
    
    public void setLowestScore(Integer lowestScore) {
        this.lowestScore = lowestScore;
    }
    
    public Long getTotalDuration() {
        return totalDuration;
    }
    
    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }
    
    public String getTotalScores() {
        return totalScores;
    }
    
    public void setTotalScores(String totalScores) {
        this.totalScores = totalScores;
    }
    
    public List<ParticipantScoreInfo> getParticipantScores() {
        return participantScores;
    }
    
    public void setParticipantScores(List<ParticipantScoreInfo> participantScores) {
        this.participantScores = participantScores;
    }
    
    public Long getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(Long completionTime) {
        this.completionTime = completionTime;
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
}
