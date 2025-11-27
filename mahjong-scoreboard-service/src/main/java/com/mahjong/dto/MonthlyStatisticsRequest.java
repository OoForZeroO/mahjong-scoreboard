package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 月度统计请求模型
 */
public class MonthlyStatisticsRequest {
    
    @JsonProperty("year")
    private Integer year; // 年份（格式：YYYY，如 2024）
    
    @JsonProperty("month")
    private Integer month; // 月份（格式：MM，如 12）
    
    // 构造函数
    public MonthlyStatisticsRequest() {}
    
    public MonthlyStatisticsRequest(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }
    
    // Getter和Setter
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
}

