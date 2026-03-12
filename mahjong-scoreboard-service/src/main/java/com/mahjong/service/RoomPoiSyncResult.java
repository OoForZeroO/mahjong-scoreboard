package com.mahjong.service;

import java.util.Objects;

/**
 * 高德 POI 同步结果。
 */
public class RoomPoiSyncResult {

    /** 本次新增落库数量 */
    private final int synced;
    /** 是否已配置高德 Key 并成功请求 */
    private final boolean success;
    /** 说明信息（如未配置 key、网络错误等） */
    private final String message;

    public RoomPoiSyncResult(int synced, boolean success, String message) {
        this.synced = synced;
        this.success = success;
        this.message = message == null ? "" : message;
    }

    public static RoomPoiSyncResult notConfigured(String message) {
        return new RoomPoiSyncResult(0, false, message);
    }

    public static RoomPoiSyncResult ok(int synced) {
        return new RoomPoiSyncResult(synced, true, "ok");
    }

    public static RoomPoiSyncResult error(String message) {
        return new RoomPoiSyncResult(0, false, message);
    }

    public int getSynced() { return synced; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomPoiSyncResult that = (RoomPoiSyncResult) o;
        return synced == that.synced && success == that.success && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(synced, success, message);
    }
}
