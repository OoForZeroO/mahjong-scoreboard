package com.mahjong.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.dto.WechatLoginResponse;
import com.mahjong.model.WechatUser;
import com.mahjong.repository.WechatUserRepository;
import com.mahjong.service.WechatAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 小程序登录：code 换 openid（调微信 code2Session），按 openid 查/建用户，返回 wechatUserId。
 */
@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    private static final Logger logger = LoggerFactory.getLogger(WechatAuthServiceImpl.class);
    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${wechat.appId:}")
    private String appId;

    @Value("${wechat.appSecret:}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WechatUserRepository wechatUserRepository;

    public WechatAuthServiceImpl(WechatUserRepository wechatUserRepository) {
        this.wechatUserRepository = wechatUserRepository;
    }

    @Override
    public WechatLoginResponse loginByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("code 不能为空");
        }
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            logger.error("微信小程序配置未设置：appId 或 appSecret 为空");
            throw new IllegalStateException("微信小程序配置未设置，请联系管理员");
        }

        String url = CODE2SESSION_URL + "?appid=" + appId + "&secret=" + appSecret
                + "&js_code=" + code.trim() + "&grant_type=authorization_code";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("微信 code2Session 返回为空");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                int errcode = root.get("errcode").asInt();
                String errmsg = root.has("errmsg") ? root.get("errmsg").asText() : "";
                logger.warn("微信 code2Session 失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new IllegalArgumentException("登录失败: " + errmsg + " (errcode=" + errcode + ")");
            }
            if (!root.has("openid")) {
                throw new IllegalArgumentException("微信未返回 openid");
            }
            String openid = root.get("openid").asText();
            WechatUser user = findOrCreateByOpenid(openid);
            return new WechatLoginResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getAvatar(),
                    user.getIsVisitor()
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("解析 code2Session 响应失败: {}", body, e);
            throw new IllegalArgumentException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 按 openid（存 wechat_users.user_id）查用户，不存在则新建。
     */
    private WechatUser findOrCreateByOpenid(String openid) {
        WechatUser user = wechatUserRepository.findByUserId(openid);
        if (user != null) {
            logger.debug("已存在用户 openid={}, wechatUserId={}", openid, user.getId());
            return user;
        }
        user = new WechatUser();
        user.setUserId(openid);
        user.setNickname("微信用户");
        user.setIsVisitor(true);
        user = wechatUserRepository.save(user);
        logger.info("新建微信用户 openid={}, wechatUserId={}", openid, user.getId());
        return user;
    }
}
