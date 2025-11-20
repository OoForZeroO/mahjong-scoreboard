package com.mahjong.controller;

import com.mahjong.model.User;
import com.mahjong.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@CrossOrigin
public class UserApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private UserService userService;

    /**
     * 创建新用户
     * 支持前端页面和微信小程序调用
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            logger.info("创建用户请求: 用户名={}, 手机号={}", request.getUsername(), request.getPhone());

            // 检查手机号是否已存在
            if (userService.existsByPhone(request.getPhone())) {
                logger.warn("手机号已存在: {}", request.getPhone());
                return buildErrorResponse(HttpStatus.CONFLICT, "手机号已被注册");
            }

            // 创建用户对象
            User user = new User();
            try {
                // 使用反射设置字段值，兼容没有setter的情况
                setUserFields(user, request);
            } catch (Exception e) {
                logger.error("设置用户字段失败: {}", e.getMessage());
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统错误: 无法处理请求数据");
            }

            // 保存用户
            User createdUser = userService.createUser(user);
            logger.info("用户创建成功: ID={}, 用户名={}", getUserFieldValue(createdUser, "id"), request.getUsername());

            // 返回用户信息
            UserResponse response = new UserResponse();
            response.setId(getUserFieldValue(createdUser, "id"));
            response.setUsername(request.getUsername());
            response.setPhone(request.getPhone());
            response.setEmail(request.getEmail());
            response.setRole(request.getRole() != null ? request.getRole() : "user");
            response.setStatus(request.getStatus() != null ? request.getStatus() : "active");
            response.setAvatar(request.getAvatar());

            return ResponseEntity.status(HttpStatus.CREATED).body(buildSuccessResponse(response));
        } catch (Exception e) {
            logger.error("创建用户异常: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表（支持分页）
     */
    @GetMapping
    public ResponseEntity<?> getUsers(@RequestParam(defaultValue = "1") Integer page, 
                                    @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 记录请求参数
            logger.info("收到分页获取用户列表请求: page={}, limit={}", page, limit);
            // 获取所有用户（后续可实现真正的分页逻辑）
            List<User> users = userService.getAllUsers();
            logger.info("查询到{}个用户", users.size());
            
            // 将User对象转换为UserResponse对象，确保正确序列化
            List<UserResponse> responseList = new ArrayList<>();
            for (User user : users) {
                try {
                    UserResponse response = new UserResponse();
                    Object idValue = getUserFieldValue(user, "id");
                    if (idValue != null) {
                        response.setId(idValue);
                    }
                    Object usernameValue = getUserFieldValue(user, "username");
                    if (usernameValue != null) {
                        response.setUsername(usernameValue.toString());
                    }
                    Object phoneValue = getUserFieldValue(user, "phone");
                    if (phoneValue != null) {
                        response.setPhone(phoneValue.toString());
                    }
                    Object emailValue = getUserFieldValue(user, "email");
                    if (emailValue != null) {
                        response.setEmail(emailValue.toString());
                    }
                    Object roleValue = getUserFieldValue(user, "role");
                    if (roleValue != null) {
                        response.setRole(roleValue.toString());
                    }
                    Object statusValue = getUserFieldValue(user, "status");
                    if (statusValue != null) {
                        response.setStatus(statusValue.toString());
                    }
                    Object avatarValue = getUserFieldValue(user, "avatar");
                    if (avatarValue != null) {
                        response.setAvatar(avatarValue.toString());
                    }
                    responseList.add(response);
                } catch (Exception e) {
                    logger.error("转换用户数据失败: {}", e.getMessage(), e);
                }
            }
            
            return ResponseEntity.ok(buildSuccessResponse(responseList));
        } catch (Exception e) {
            logger.error("获取用户列表异常: {}", e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据手机号查询用户
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<?> getUserByPhone(@PathVariable @NotBlank String phone) {
        try {
            logger.info("根据手机号查询用户: {}", phone);
            User user = userService.getUserByPhone(phone);
            if (user == null) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "用户不存在");
            }
            return ResponseEntity.ok(buildSuccessResponse(user));
        } catch (Exception e) {
            logger.error("查询用户异常: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查询用户失败");
        }
    }

    /**
     * 检查手机号是否已被使用
     */
    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<?> checkPhoneExists(@PathVariable @NotBlank String phone) {
        try {
            logger.info("检查手机号是否存在: {}", phone);
            boolean exists = userService.existsByPhone(phone);
            return ResponseEntity.ok(buildSuccessResponse(Map.of("exists", exists)));
        } catch (Exception e) {
            logger.error("检查手机号异常: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统错误");
        }
    }

    // 辅助方法
    private void setUserFields(User user, UserCreateRequest request) throws Exception {
        // 设置用户名
        java.lang.reflect.Field usernameField = User.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        usernameField.set(user, request.getUsername());

        // 设置手机号
        java.lang.reflect.Field phoneField = User.class.getDeclaredField("phone");
        phoneField.setAccessible(true);
        phoneField.set(user, request.getPhone());

        // 设置邮箱（可选）
        if (request.getEmail() != null) {
            java.lang.reflect.Field emailField = User.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(user, request.getEmail());
        }

        // 设置密码
        java.lang.reflect.Field passwordField = User.class.getDeclaredField("password");
        passwordField.setAccessible(true);
        passwordField.set(user, request.getPassword());

        // 设置角色
        java.lang.reflect.Field roleField = User.class.getDeclaredField("role");
        roleField.setAccessible(true);
        roleField.set(user, request.getRole() != null ? request.getRole() : "user");

        // 设置状态
        java.lang.reflect.Field statusField = User.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(user, request.getStatus() != null ? request.getStatus() : "active");

        // 设置头像（可选）
        if (request.getAvatar() != null) {
            java.lang.reflect.Field avatarField = User.class.getDeclaredField("avatar");
            avatarField.setAccessible(true);
            avatarField.set(user, request.getAvatar());
        }
    }

    private Object getUserFieldValue(User user, String fieldName) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(user);
        } catch (Exception e) {
            logger.error("获取字段值失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }

    // 请求参数模型
    static class UserCreateRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        private String email;

        @NotBlank(message = "密码不能为空")
        private String password;

        private String role = "user";

        private String status = "active";

        private String avatar;

        // getter和setter
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

    // 响应模型
    static class UserResponse {
        private Object id;
        private String username;
        private String phone;
        private String email;
        private String role;
        private String status;
        private String avatar;

        // getter和setter
        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

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