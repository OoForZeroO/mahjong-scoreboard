package com.mahjong.model;

import jakarta.persistence.*;

/**
 * 用户月度统计预聚合：按 user_id + year_month 更新，便于本月/本年快速查询。
 */
@Entity
@Table(name = "user_monthly_stats")
public class UserMonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "year_month", nullable = false)
    private Integer yearMonth;

    @Column(name = "total_matches", nullable = false)
    private Integer totalMatches = 0;

    @Column(name = "win_matches", nullable = false)
    private Integer winMatches = 0;

    @Column(name = "lose_matches", nullable = false)
    private Integer loseMatches = 0;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "total_multiplier_score", nullable = false)
    private Double totalMultiplierScore = 0.0;

    @Column(name = "win_total_score", nullable = false)
    private Integer winTotalScore = 0;

    @Column(name = "win_total_multiplier_score", nullable = false)
    private Double winTotalMultiplierScore = 0.0;

    @Column(name = "lose_total_score", nullable = false)
    private Integer loseTotalScore = 0;

    @Column(name = "lose_total_multiplier_score", nullable = false)
    private Double loseTotalMultiplierScore = 0.0;

    @Column(name = "create_time", updatable = false)
    private Long createTime;

    @Column(name = "update_time")
    private Long updateTime;

    @PrePersist
    public void prePersist() {
        long now = System.currentTimeMillis();
        if (createTime == null) createTime = now;
        if (updateTime == null) updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getYearMonth() { return yearMonth; }
    public void setYearMonth(Integer yearMonth) { this.yearMonth = yearMonth; }
    public Integer getTotalMatches() { return totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }
    public Integer getWinMatches() { return winMatches; }
    public void setWinMatches(Integer winMatches) { this.winMatches = winMatches; }
    public Integer getLoseMatches() { return loseMatches; }
    public void setLoseMatches(Integer loseMatches) { this.loseMatches = loseMatches; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public Double getTotalMultiplierScore() { return totalMultiplierScore; }
    public void setTotalMultiplierScore(Double totalMultiplierScore) { this.totalMultiplierScore = totalMultiplierScore; }
    public Integer getWinTotalScore() { return winTotalScore; }
    public void setWinTotalScore(Integer winTotalScore) { this.winTotalScore = winTotalScore; }
    public Double getWinTotalMultiplierScore() { return winTotalMultiplierScore; }
    public void setWinTotalMultiplierScore(Double winTotalMultiplierScore) { this.winTotalMultiplierScore = winTotalMultiplierScore; }
    public Integer getLoseTotalScore() { return loseTotalScore; }
    public void setLoseTotalScore(Integer loseTotalScore) { this.loseTotalScore = loseTotalScore; }
    public Double getLoseTotalMultiplierScore() { return loseTotalMultiplierScore; }
    public void setLoseTotalMultiplierScore(Double loseTotalMultiplierScore) { this.loseTotalMultiplierScore = loseTotalMultiplierScore; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }
}
