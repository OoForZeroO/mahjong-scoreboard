package com.mahjong.controller;

import com.mahjong.dto.MonthlyStatisticsRequest;
import com.mahjong.dto.MonthlyStatisticsResponse;
import com.mahjong.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计查询控制器
 */
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取用户月度统计数据
     * 
     * @param wechatUserId 微信用户ID
     * @param request 请求体，包含year和month
     * @return 月度统计数据
     */
    @PostMapping("/users/{wechatUserId}/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics(
            @PathVariable String wechatUserId,
            @RequestBody MonthlyStatisticsRequest request) {
        try {
            if (request == null || request.getYear() == null) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "请求参数错误：year不能为空");
            }
            
            logger.info("获取用户月度统计，wechatUserId: {}, year: {}, month: {}", 
                       wechatUserId, request.getYear(), request.getMonth());
            MonthlyStatisticsResponse statistics = statisticsService.getMonthlyStatistics(
                wechatUserId, request.getYear(), request.getMonth());
            // 始终返回200，即使数据为空也返回完整结构
            return ResponseEntity.ok(buildSuccessResponse(statistics));
        } catch (Exception e) {
            logger.error("获取月度统计失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取月度统计失败: " + e.getMessage());
        }
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

