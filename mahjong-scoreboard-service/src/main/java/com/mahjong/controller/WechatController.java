package com.mahjong.controller;

import com.mahjong.service.WechatQRCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wechat")
@CrossOrigin
public class WechatController {

    private static final Logger logger = LoggerFactory.getLogger(WechatController.class);

    @Autowired
    private WechatQRCodeService wechatQRCodeService;

    /**
     * 获取微信 access_token
     * 
     * @return access_token 和 expires_in
     */
    @GetMapping("/access-token")
    public ResponseEntity<?> getAccessToken() {
        logger.info("收到获取微信access_token请求");
        try {
            Map<String, Object> tokenData = wechatQRCodeService.getAccessToken();
            if (tokenData == null || tokenData.isEmpty()) {
                logger.warn("获取access_token返回数据为空");
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取access_token失败：返回数据为空");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", tokenData);
            
            logger.info("成功获取access_token，expires_in: {}秒", tokenData.get("expires_in"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("获取access_token失败: {}", e.getMessage(), e);
            String errorMessage = e.getMessage();
            // 如果是配置问题，返回更友好的错误信息
            if (errorMessage != null && errorMessage.contains("配置未设置")) {
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "微信小程序配置未设置，请联系管理员");
            }
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取access_token失败: " + errorMessage);
        } catch (Exception e) {
            logger.error("获取access_token异常", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取access_token失败: " + e.getMessage());
        }
    }

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}
