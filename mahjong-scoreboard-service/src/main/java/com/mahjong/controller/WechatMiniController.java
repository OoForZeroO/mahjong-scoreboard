package com.mahjong.controller;

import com.mahjong.model.WechatUser;
import com.mahjong.service.WechatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wechat-users")
@CrossOrigin
public class WechatMiniController {

    @Autowired
    private WechatUserService service;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody WechatUser user) {
        try {
            WechatUser saved = service.createWechatUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建微信用户失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", service.getAllWechatUsers());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取微信用户列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据主键 id 查询微信用户。
     * 路径参数可为数字（wechat_users.id）或临时占位（如 temp_xxx）。非数字或 temp_ 前缀时返回 data: null，便于前端在未登录时统一走 code2session 登录。
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 400);
            err.put("message", "id 不能为空");
            err.put("data", null);
            return ResponseEntity.badRequest().body(err);
        }
        if (id.startsWith("temp_") || !id.trim().matches("\\d+")) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", null);
            return ResponseEntity.ok(response);
        }
        try {
            Long idLong = Long.parseLong(id.trim());
            return service.getWechatUserById(idLong)
                    .map(user -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "success");
                        response.put("data", user);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "success");
                        response.put("data", null);
                        return ResponseEntity.ok(response);
                    });
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取微信用户失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<?> getUserByUserId(@PathVariable String userId) {
        try {
            WechatUser user = service.getWechatUserByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取微信用户失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody WechatUser user) {
        try {
            WechatUser updated = service.updateWechatUser(id, user);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新微信用户失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            service.deleteWechatUser(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除微信用户失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}