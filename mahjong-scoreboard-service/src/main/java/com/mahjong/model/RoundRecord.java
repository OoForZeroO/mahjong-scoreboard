package com.mahjong.model;

import jakarta.persistence.*;
@Entity
@Table(name = "round_records")
public class RoundRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private MatchParticipant participant;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "score_change", nullable = false)
    private Integer scoreChange;

    @Column(name = "cumulative_score", nullable = false)
    private Integer cumulativeScore;

    @Column(name = "win_type", length = 50)
    private String winType;

    @Column(name = "loser_ids", length = 255)
    private String loserIds;

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

    public MatchParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(MatchParticipant participant) {
        this.participant = participant;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getScoreChange() {
        return scoreChange;
    }

    public void setScoreChange(Integer scoreChange) {
        this.scoreChange = scoreChange;
    }

    public Integer getCumulativeScore() {
        return cumulativeScore;
    }

    public void setCumulativeScore(Integer cumulativeScore) {
        this.cumulativeScore = cumulativeScore;
    }

    public String getWinType() {
        return winType;
    }

    public void setWinType(String winType) {
        this.winType = winType;
    }

    public String getLoserIds() {
        return loserIds;
    }

    public void setLoserIds(String loserIds) {
        this.loserIds = loserIds;
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