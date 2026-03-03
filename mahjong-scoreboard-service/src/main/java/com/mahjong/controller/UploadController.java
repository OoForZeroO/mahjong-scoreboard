package com.mahjong.controller;

import com.mahjong.service.AvatarUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传接口：小程序选图后通过 wx.uploadFile 将文件上传到本接口，
 * 接口将文件保存到服务器并返回可公网访问的 https 地址，用于替换 wxfile://、http(s)://tmp/ 等临时地址。
 */
@RestController
@RequestMapping("/api/v1/upload")
@CrossOrigin
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private AvatarUploadService avatarUploadService;

    /**
     * 上传头像图片。
     * 小程序端：wx.chooseMedia 选图后，用 wx.uploadFile 上传，filePath 为临时路径；
     * 接口保存到服务器并返回可公网访问的 URL，前端将该 URL 用于用户/参与者头像字段。
     *
     * @param file 表单字段名需为 file（wx.uploadFile 默认即 file）
     * @return 成功时 data 为头像 URL（https）；失败时返回 4xx/5xx 及 message
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String url = avatarUploadService.uploadAvatar(file);
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            return ResponseEntity.ok(buildSuccessResponse(data));
        } catch (IllegalArgumentException e) {
            logger.warn("头像上传参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("头像上传失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "头像上传失败: " + e.getMessage());
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
}
