package com.mahjong.controller;

import com.mahjong.model.User;
import com.mahjong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 创建用户
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        try {
            // 确保设置默认值（通过反射）
            try {
                java.lang.reflect.Field roleField = User.class.getDeclaredField("role");
                roleField.setAccessible(true);
                if (roleField.get(user) == null) {
                    roleField.set(user, "user");
                }
                
                java.lang.reflect.Field statusField = User.class.getDeclaredField("status");
                statusField.setAccessible(true);
                if (statusField.get(user) == null) {
                    statusField.set(user, "active");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(buildSuccessResponse(convertToUserResponse(createdUser)));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "创建用户失败: " + e.getMessage());
        }
    }

    // 获取所有用户
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> userList = users.stream()
                    .map(this::convertToUserResponse)
                    .toList();
            return ResponseEntity.ok(buildSuccessResponse(userList));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取用户列表失败: " + e.getMessage());
        }
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            return userService.getUserById(id)
                    .map(user -> ResponseEntity.ok(buildSuccessResponse(convertToUserResponse(user))))
                    .orElse(buildErrorResponse(HttpStatus.NOT_FOUND, "用户不存在"));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取用户失败: " + e.getMessage());
        }
    }

    // 根据手机号获取用户
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getUserByPhone(@PathVariable String phone) {
        try {
            User user = userService.getUserByPhone(phone);
            if (user != null) {
                return ResponseEntity.ok(buildSuccessResponse(convertToUserResponse(user)));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "用户不存在");
            }
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取用户失败: " + e.getMessage());
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(buildSuccessResponse(convertToUserResponse(updatedUser)));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新用户失败: " + e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(buildSuccessResponse("用户删除成功"));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "删除用户失败: " + e.getMessage());
        }
    }

    // 检查手机号是否存在
    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Map<String, Object>> existsByPhone(@PathVariable String phone) {
        try {
            boolean exists = userService.existsByPhone(phone);
            return ResponseEntity.ok(buildSuccessResponse(exists));
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "检查手机号失败: " + e.getMessage());
        }
    }

    // 辅助方法：转换User对象为响应格式（不包含密码）
    private Map<String, Object> convertToUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("phone", user.getPhone());
        userResponse.put("email", user.getEmail());
        userResponse.put("role", user.getRole());
        userResponse.put("status", user.getStatus());
        userResponse.put("avatar", user.getAvatar());
        userResponse.put("createTime", user.getCreateTime());
        userResponse.put("updateTime", user.getUpdateTime());
        // 注意：不包含password字段，确保安全性
        return userResponse;
    }

    // 辅助方法：构建成功响应
    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    // 辅助方法：构建错误响应
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}