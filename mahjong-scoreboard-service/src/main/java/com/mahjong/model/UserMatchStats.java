package com.mahjong.model;

import jakarta.persistence.*;

/**
 * 用户对局结果汇总：每局每用户一条，用于按用户+时间范围统计。
 * 与 match_results 同时在对局结束时写入。
 */
@Entity
@Table(name = "user_match_stats")
public class UserMatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "participant_id")
    private Long participantId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    @Column(name = "settlement_multiplier", nullable = false)
    private Double settlementMultiplier = 1.0;

    @Column(name = "is_winner", nullable = false)
    private Boolean isWinner = false;

    @Column(name = "match_end_time", nullable = false)
    private Long matchEndTime;

    @Column(name = "create_time", updatable = false)
    private Long createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = System.currentTimeMillis();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public Integer getFinalScore() { return finalScore; }
    public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }
    public Double getSettlementMultiplier() { return settlementMultiplier; }
    public void setSettlementMultiplier(Double settlementMultiplier) { this.settlementMultiplier = settlementMultiplier; }
    public Boolean getIsWinner() { return isWinner; }
    public void setIsWinner(Boolean isWinner) { this.isWinner = isWinner; }
    public Long getMatchEndTime() { return matchEndTime; }
    public void setMatchEndTime(Long matchEndTime) { this.matchEndTime = matchEndTime; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
}
