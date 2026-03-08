package com.mahjong.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 对局详情响应模型
 */
public class MatchDetailResponse {
    
    // 对局基本信息
    @JsonProperty("matchId")
    private Long matchId;
    
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("totalRounds")
    private Integer totalRounds;
    
    @JsonProperty("currentRound")
    private Integer currentRound;
    
    @JsonProperty("matchStatus")
    private Integer matchStatus; // 0:进行中, 1:已完成
    
    @JsonProperty("settlementMultiplier")
    private Double settlementMultiplier; // 收盘倍率
    
    @JsonProperty("createTime")
    private Long createTime;
    
    @JsonProperty("updateTime")
    private Long updateTime;
    
    // 轮次数据列表
    @JsonProperty("rounds")
    private List<RoundDetail> rounds;
    
    // 参与者数据列表
    @JsonProperty("participants")
    private List<ParticipantDetail> participants;
    
    // 构造函数
    public MatchDetailResponse() {}
    
    // Getter和Setter
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
    
    public Integer getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
    
    public Integer getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }
    
    public Integer getMatchStatus() {
        return matchStatus;
    }
    
    public void setMatchStatus(Integer matchStatus) {
        this.matchStatus = matchStatus;
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
    
    public List<RoundDetail> getRounds() {
        return rounds;
    }
    
    public void setRounds(List<RoundDetail> rounds) {
        this.rounds = rounds;
    }
    
    public List<ParticipantDetail> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantDetail> participants) {
        this.participants = participants;
    }
    
    /**
     * 轮次详情内部类
     */
    public static class RoundDetail {
        @JsonProperty("roundNumber")
        private Integer roundNumber;
        
        @JsonProperty("roundTime")
        private Long roundTime;
        
        @JsonProperty("scores")
        private List<RoundScoreDetail> scores;
        
        // 构造函数
        public RoundDetail() {}
        
        public RoundDetail(Integer roundNumber, Long roundTime) {
            this.roundNumber = roundNumber;
            this.roundTime = roundTime;
        }
        
        // Getter和Setter
        public Integer getRoundNumber() {
            return roundNumber;
        }
        
        public void setRoundNumber(Integer roundNumber) {
            this.roundNumber = roundNumber;
        }
        
        public Long getRoundTime() {
            return roundTime;
        }
        
        public void setRoundTime(Long roundTime) {
            this.roundTime = roundTime;
        }
        
        public List<RoundScoreDetail> getScores() {
            return scores;
        }
        
        public void setScores(List<RoundScoreDetail> scores) {
            this.scores = scores;
        }
    }
    
    /**
     * 轮次得分详情内部类
     */
    public static class RoundScoreDetail {
        @JsonProperty("participantId")
        private Long participantId;
        
        @JsonProperty("participantName")
        private String participantName;
        
        @JsonProperty("score")
        private Integer score;
        
        @JsonProperty("cumulativeScore")
        private Integer cumulativeScore;
        
        // 构造函数
        public RoundScoreDetail() {}
        
        public RoundScoreDetail(Long participantId, String participantName, Integer score, Integer cumulativeScore) {
            this.participantId = participantId;
            this.participantName = participantName;
            this.score = score;
            this.cumulativeScore = cumulativeScore;
        }
        
        // Getter和Setter
        public Long getParticipantId() {
            return participantId;
        }
        
        public void setParticipantId(Long participantId) {
            this.participantId = participantId;
        }
        
        public String getParticipantName() {
            return participantName;
        }
        
        public void setParticipantName(String participantName) {
            this.participantName = participantName;
        }
        
        public Integer getScore() {
            return score;
        }
        
        public void setScore(Integer score) {
            this.score = score;
        }
        
        public Integer getCumulativeScore() {
            return cumulativeScore;
        }
        
        public void setCumulativeScore(Integer cumulativeScore) {
            this.cumulativeScore = cumulativeScore;
        }
    }
}
