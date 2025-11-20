package com.mahjong.service;

/**
 * 数据清理服务接口
 */
public interface CleanupService {
    /**
     * 清理24小时前状态为2（已取消）的对局数据及其关联的参与者数据
     */
    void cleanupCancelledMatches();
}

