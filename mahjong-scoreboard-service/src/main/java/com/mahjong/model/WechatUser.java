package com.mahjong.model;

import jakarta.persistence.*;

@Entity
@Table(name = "wechat_users")
public class WechatUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 历史字段：最初用于存 openid，现在仍保持为 openid，供旧逻辑兼容使用。
     */
    @Column(name = "user_id", nullable = false, length = 100, unique = true)
    private String userId;

    /**
     * 微信小程序 appId，用于和 openid 组合生成身份键。
     */
    @Column(name = "app_id", length = 64)
    private String appId;

    /**
     * 微信返回的 openid。
     */
    @Column(name = "openid", length = 100)
    private String openid;

    /**
     * 微信返回的 unionid（如有）。
     */
    @Column(name = "unionid", length = 100)
    private String unionid;

    /**
     * 统一身份键：
     * - 有 unionid: unionid:{unionid}
     * - 否则: openid:{appId}:{openid}
     */
    @Column(name = "identity_key", length = 200, unique = true)
    private String identityKey;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname; // 用户昵称

    @Column(name = "username", length = 100)
    private String username; // 用户名称

    @Column(name = "avatar", length = 500)
    private String avatar; // 用户头像

    @Column(name = "is_visitor", nullable = false)
    private Boolean isVisitor = false; // 是否游客

    @Column(name = "last_login_at")
    private Long lastLoginAt; // 最近登录时间

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsVisitor() {
        return isVisitor;
    }

    public void setIsVisitor(Boolean isVisitor) {
        this.isVisitor = isVisitor;
    }

    public Long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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