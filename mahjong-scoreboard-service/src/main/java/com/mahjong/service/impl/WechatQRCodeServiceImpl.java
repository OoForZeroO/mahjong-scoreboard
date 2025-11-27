package com.mahjong.service.impl;

import com.mahjong.service.WechatQRCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

/**
 * 微信二维码生成服务实现
 */
@Service
public class WechatQRCodeServiceImpl implements WechatQRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(WechatQRCodeServiceImpl.class);

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    // 使用 getwxacodeunlimit 接口（支持不限制数量的二维码）
    private static final String QR_CODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";

    @Value("${wechat.appId:}")
    private String appId;

    @Value("${wechat.appSecret:}")
    private String appSecret;

    @Value("${wechat.qrcode.page:pages/gameDetail/gameDetail}")
    private String qrcodePage;

    @Value("${wechat.qrcode.width:430}")
    private Integer qrcodeWidth;

    @Value("${wechat.qrcode.checkPath:false}")
    private Boolean checkPath;

    @Value("${wechat.qrcode.envVersion:develop}")
    private String envVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    // 缓存access_token，避免频繁请求
    private String cachedAccessToken;
    private Long tokenExpireTime;

    @Override
    public String generateMatchQRCode(Long matchId) {
        try {
            logger.info("开始生成对局二维码，matchId: {}", matchId);

            // 1. 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                logger.error("获取微信access_token失败");
                throw new RuntimeException("获取微信access_token失败，请检查配置");
            }

            // 2. 调用微信API生成二维码
            String scene = "matchId=" + matchId;
            if (scene.length() > 32) {
                scene = scene.substring(0, 32); // 限制最大32字符
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("scene", scene);
            requestBody.put("page", qrcodePage);
            requestBody.put("width", qrcodeWidth);
            requestBody.put("check_path", checkPath);
            requestBody.put("env_version", envVersion);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String url = QR_CODE_URL + "?access_token=" + accessToken;
            logger.info("调用微信API生成二维码，URL: {}", url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] imageData = response.getBody();
                
                // 检查是否是错误响应（微信API错误时返回JSON）
                String responseStr = new String(imageData);
                if (responseStr.startsWith("{") && responseStr.contains("errcode")) {
                    logger.error("微信API返回错误: {}", responseStr);
                    throw new RuntimeException("生成二维码失败: " + responseStr);
                }

                // 转换为Base64
                String base64 = Base64.getEncoder().encodeToString(imageData);
                String qrcodeData = "data:image/png;base64," + base64;
                
                logger.info("二维码生成成功，matchId: {}", matchId);
                return qrcodeData;
            } else {
                logger.error("调用微信API失败，状态码: {}", response.getStatusCode());
                throw new RuntimeException("调用微信API失败");
            }

        } catch (Exception e) {
            logger.error("生成二维码失败", e);
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取微信access_token
     */
    private String getAccessToken() {
        try {
            // 检查缓存是否有效（提前5分钟刷新）
            if (cachedAccessToken != null && tokenExpireTime != null && 
                System.currentTimeMillis() < tokenExpireTime - 5 * 60 * 1000) {
                logger.debug("使用缓存的access_token");
                return cachedAccessToken;
            }

            logger.info("获取新的access_token");

            if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
                logger.error("微信小程序配置未设置：appId={}, appSecret={}", appId, appSecret != null ? "已设置" : "未设置");
                throw new RuntimeException("微信小程序配置未设置，请在application.properties中配置wechat.appId和wechat.appSecret");
            }

            String url = ACCESS_TOKEN_URL + "?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                if (body != null && body.containsKey("errcode")) {
                    logger.error("获取access_token失败: {}", body);
                    throw new RuntimeException("获取access_token失败: " + body.get("errmsg"));
                }
                
                if (body != null) {
                    String accessToken = (String) body.get("access_token");
                    Object expiresInObj = body.get("expires_in");
                    Integer expiresIn = expiresInObj instanceof Integer ? (Integer) expiresInObj : 
                                       (expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 7200);
                    
                    if (accessToken != null && !accessToken.isEmpty()) {
                        cachedAccessToken = accessToken;
                        tokenExpireTime = System.currentTimeMillis() + (expiresIn != null ? expiresIn * 1000L : 7200000L);
                        logger.info("成功获取access_token，有效期: {}秒", expiresIn);
                        return accessToken;
                    }
                }
            }
            
            throw new RuntimeException("获取access_token失败：响应为空");
        } catch (Exception e) {
            logger.error("获取access_token失败", e);
            throw new RuntimeException("获取access_token失败: " + e.getMessage(), e);
        }
    }
}

