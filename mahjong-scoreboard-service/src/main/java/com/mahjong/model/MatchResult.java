package com.mahjong.model;

import jakarta.persistence.*;
@Entity
@Table(name = "match_results")
public class MatchResult {
    @Id
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    // 完全移除@OneToOne关系，避免Hibernate同步问题
    // 如果需要获取Match对象，可以通过matchId单独查询

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private MatchParticipant winner;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "lowest_score")
    private Integer lowestScore;

    @Column(name = "total_duration")
    private Long totalDuration;

    @Column(name = "total_scores", columnDefinition = "TEXT")
    private String totalScores;

    @Column(name = "completion_time")
    private Long completionTime;

    // 移除total_rounds字段，不需要在结果表中存储
    // @Column(name = "total_rounds")
    // private Integer totalRounds;

    @Column(updatable = false)
    private Long createTime;

    @Column
    private Long updateTime;

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
    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    // 移除match相关的getter和setter方法
    // 如果需要获取Match对象，可以通过matchId单独查询

    public MatchParticipant getWinner() {
        return winner;
    }

    public void setWinner(MatchParticipant winner) {
        this.winner = winner;
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

    public Long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Long completionTime) {
        this.completionTime = completionTime;
    }

    // 移除totalRounds字段的getter和setter方法
    // public Integer getTotalRounds() {
    //     return totalRounds;
    // }

    // public void setTotalRounds(Integer totalRounds) {
    //     this.totalRounds = totalRounds;
    // }

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