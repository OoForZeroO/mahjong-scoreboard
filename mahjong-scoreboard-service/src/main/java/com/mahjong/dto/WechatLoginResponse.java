package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序 code 换登录态后的返回：后端自己的 wechatUserId 及用户信息，供前端缓存使用。
 * 不返回 openid/session_key，前端仅使用 wechatUserId 识别用户。
 */
public class WechatLoginResponse {

    /** 后端用户标识，即 wechat_users.id，后续请求参与对局等均用此 id */
    @JsonProperty("wechatUserId")
    private Long wechatUserId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("isVisitor")
    private Boolean isVisitor;

    public WechatLoginResponse() {}

    public WechatLoginResponse(Long wechatUserId, String nickname, String avatar, Boolean isVisitor) {
        this.wechatUserId = wechatUserId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.isVisitor = isVisitor != null ? isVisitor : true;
    }

    public Long getWechatUserId() { return wechatUserId; }
    public void setWechatUserId(Long wechatUserId) { this.wechatUserId = wechatUserId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Boolean getIsVisitor() { return isVisitor; }
    public void setIsVisitor(Boolean isVisitor) { this.isVisitor = isVisitor; }
}
