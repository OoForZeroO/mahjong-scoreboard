package com.mahjong.service;

import com.mahjong.dto.WechatLoginResponse;

/**
 * 小程序登录：用 wx.login 的 code 换 openid，并查/建用户，返回后端 wechatUserId 及用户信息。
 */
public interface WechatAuthService {

    /**
     * 用小程序传来的 code 调微信 code2Session 拿 openid，按 openid 查或建 wechat_users，
     * 返回本系统的 wechatUserId（wechat_users.id）及用户信息，不返回 openid。
     *
     * @param code 小程序 wx.login() 得到的 code
     * @return 登录结果（wechatUserId、nickname、avatar、isVisitor）
     * @throws IllegalArgumentException code 为空或微信返回错误
     */
    WechatLoginResponse loginByCode(String code);
}
