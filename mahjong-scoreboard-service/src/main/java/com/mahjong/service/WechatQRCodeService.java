package com.mahjong.service;

import java.util.Map;

/**
 * 微信二维码生成服务接口
 */
public interface WechatQRCodeService {
    /**
     * 生成对局二维码
     * 
     * @param matchId 对局ID
     * @return Base64格式的二维码数据（data:image/png;base64,xxxxx）
     */
    String generateMatchQRCode(Long matchId);

    /**
     * 根据对局信息生成标准方形二维码（与小程序码信息一致，内容为 matchId）
     * 用于对局详情页同时展示小程序码与标准二维码。
     *
     * @param matchId 对局ID
     * @return Base64格式的二维码数据（data:image/png;base64,xxxxx）
     */
    String generateStandardQRCode(Long matchId);

    /**
     * 获取微信 access_token
     * 
     * @return 包含 access_token 和 expires_in 的 Map
     */
    Map<String, Object> getAccessToken();
}

