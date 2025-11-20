package com.mahjong.service;

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
}

