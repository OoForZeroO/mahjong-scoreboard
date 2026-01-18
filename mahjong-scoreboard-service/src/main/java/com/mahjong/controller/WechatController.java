package com.mahjong.controller;

import com.mahjong.service.WechatQRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wechat")
@CrossOrigin
public class WechatController {

    @Autowired
    private WechatQRCodeService wechatQRCodeService;

    /**
     * 获取微信 access_token
     * 
     * @return access_token 和 expires_in
     */
    @GetMapping("/access-token")
    public ResponseEntity<?> getAccessToken() {
        try {
            Map<String, Object> tokenData = wechatQRCodeService.getAccessToken();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", tokenData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取access_token失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
