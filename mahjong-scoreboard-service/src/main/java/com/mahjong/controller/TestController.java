package com.mahjong.controller;

import com.mahjong.model.User;
import com.mahjong.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private UserService userService;

    // 简单的测试接口，返回Hello World
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        logger.info("收到hello请求");
        return ResponseEntity.ok("Hello World!");
    }

    // 测试创建用户的接口，添加详细的错误处理
    @PostMapping("/user")
    public ResponseEntity<?> createTestUser(@RequestBody TestUserRequest request) {
        try {
            logger.info("开始创建测试用户，用户名: {}", request.getUsername());
            
            // 创建一个简单的用户对象，不依赖lombok的setter方法
            User user = new User();
            // 直接使用反射来设置字段值
            try {
                // 设置基本字段
                java.lang.reflect.Field usernameField = User.class.getDeclaredField("username");
                usernameField.setAccessible(true);
                usernameField.set(user, request.getUsername());
                
                java.lang.reflect.Field phoneField = User.class.getDeclaredField("phone");
                phoneField.setAccessible(true);
                phoneField.set(user, request.getPhone());
                
                // 设置新添加的字段
                if (request.getEmail() != null) {
                    java.lang.reflect.Field emailField = User.class.getDeclaredField("email");
                    emailField.setAccessible(true);
                    emailField.set(user, request.getEmail());
                }
                
                // 密码字段是必填的
                if (request.getPassword() != null) {
                    java.lang.reflect.Field passwordField = User.class.getDeclaredField("password");
                    passwordField.setAccessible(true);
                    passwordField.set(user, request.getPassword());
                } else {
                    // 如果没有提供密码，设置默认密码
                    java.lang.reflect.Field passwordField = User.class.getDeclaredField("password");
                    passwordField.setAccessible(true);
                    passwordField.set(user, "123456");
                }
                
                // 设置角色，默认为user
                java.lang.reflect.Field roleField = User.class.getDeclaredField("role");
                roleField.setAccessible(true);
                roleField.set(user, request.getRole() != null ? request.getRole() : "user");
                
                // 设置状态，默认为active
                java.lang.reflect.Field statusField = User.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(user, request.getStatus() != null ? request.getStatus() : "active");
                
                // 设置头像
                if (request.getAvatar() != null) {
                    java.lang.reflect.Field avatarField = User.class.getDeclaredField("avatar");
                    avatarField.setAccessible(true);
                    avatarField.set(user, request.getAvatar());
                }
                
                logger.info("成功设置用户字段值");
            } catch (Exception ex) {
                logger.error("反射设置字段失败: {}", ex.getMessage());
                // 如果反射失败，至少记录信息
            }
            
            // 调用服务层创建用户
            logger.info("调用userService.createUser");
            User createdUser = userService.createUser(user);
            logger.info("用户创建成功");
            
            // 返回成功消息，不尝试获取ID
            return ResponseEntity.ok("用户创建成功: " + request.getUsername() + " (" + request.getPhone() + ")");
        } catch (Exception e) {
            logger.error("创建用户失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("创建用户失败: " + e.getMessage());
        }
    }

    // 内部静态类，用于接收请求参数
    static class TestUserRequest {
        private String username;
        private String phone;
        private String email;
        private String password;
        private String role = "user";
        private String status = "active";
        private String avatar;
        
        // getter和setter方法
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
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getAvatar() {
            return avatar;
        }
        
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}