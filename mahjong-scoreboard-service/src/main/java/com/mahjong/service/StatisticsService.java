package com.mahjong.service;

import com.mahjong.dto.MonthlyStatisticsResponse;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    /**
     * 获取用户月度统计数据
     * 
     * @param wechatUserId 微信用户ID
     * @param year 年份（必填）
     * @param month 月份（可选，如果为null则查询全年数据）
     * @return 月度统计数据
     */
    MonthlyStatisticsResponse getMonthlyStatistics(String wechatUserId, Integer year, Integer month);
}

