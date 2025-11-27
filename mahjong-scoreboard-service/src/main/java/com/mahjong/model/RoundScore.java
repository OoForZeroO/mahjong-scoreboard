package com.mahjong.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@Entity
@Table(name = "round_scores", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "participant_id", "round_number"}))
public class RoundScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private MatchParticipant participant;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "round_time", nullable = false)
    private Long roundTime;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "cumulative_score", nullable = false)
    private Integer cumulativeScore;

    @Column(updatable = false)
    private Long createTime;

    @Column
    private Long updateTime;

    // 临时字段，用于API接收participantId
    @Transient
    @JsonProperty("participantId")
    private Long participantId;

    @PrePersist
    public void prePersist() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        if (this.roundTime == null) {
            this.roundTime = System.currentTimeMillis();
        }
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

    public Long getRoundTime() {
        return roundTime;
    }

    public void setRoundTime(Long roundTime) {
        this.roundTime = roundTime;
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

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }
}