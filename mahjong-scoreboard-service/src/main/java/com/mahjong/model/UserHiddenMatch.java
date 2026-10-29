package com.mahjong.model;

import jakarta.persistence.*;

/**
 * 用户自己隐藏的对局。只影响该用户的历史列表，不删除对局和计分。
 */
@Entity
@Table(
    name = "user_hidden_matches",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_key", "match_id"})
)
public class UserHiddenMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", nullable = false, length = 100)
    private String userKey;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

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
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
}
