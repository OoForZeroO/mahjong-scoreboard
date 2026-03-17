package com.mahjong.controller;

import com.mahjong.model.User;
import com.mahjong.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 后台平台登录接口：基于 users 表的用户名 / 手机号 + 密码登录。
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if ((request.getUsername() == null || request.getUsername().trim().isEmpty())
                && (request.getPhone() == null || request.getPhone().trim().isEmpty())) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "username 和 phone 至少填写一个");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "password 不能为空");
        }

        Optional<User> userOpt;
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            userOpt = userRepository.findByUsername(request.getUsername().trim());
        } else {
            User byPhone = userRepository.findByPhone(request.getPhone().trim());
            userOpt = Optional.ofNullable(byPhone);
        }

        if (!userOpt.isPresent()) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        User user = userOpt.get();
        String rawPassword = request.getPassword().trim();
        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        data.put("status", user.getStatus());
        data.put("token", token);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }

    public static class LoginRequest {
        private String username;
        private String phone;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

