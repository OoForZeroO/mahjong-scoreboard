package com.mahjong.service;

import com.mahjong.dto.RoomPoiItem;
import com.mahjong.dto.RoomFromPoiRequest;
import com.mahjong.model.Room;

import java.util.List;

/**
 * 从互联网（高德 POI）拉取棋牌室信息；支持「仅搜索不落库」与「选择后落库」。
 * 需配置 amap.api.key。
 */
public interface RoomPoiSyncService {

    /**
     * 从高德 POI 搜索「棋牌」相关地点，仅返回列表，不落库。
     * 供前端展示下拉列表，用户选择后再调用 saveFromPoi 落库。
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param radiusKm  半径（公里）
     * @return POI 列表（含 externalId、name、address、经纬度、distanceKm）；未配置 key 时抛 IllegalStateException
     */
    List<RoomPoiItem> searchFromPoi(double latitude, double longitude, double radiusKm);

    /**
     * 将前端选中的 POI 门店落库。若 externalId 已存在则返回已存在记录，否则新增。
     *
     * @param request 选中的门店信息（externalId 必填）
     * @return 保存或已存在的 Room
     */
    Room saveFromPoi(RoomFromPoiRequest request);

    /**
     * 按中心点坐标与半径，从高德 POI 拉取「棋牌」相关地点并写入 rooms 表（已存在则跳过）。
     * 可选的后台批量同步方式；主流程推荐使用 searchFromPoi + saveFromPoi。
     */
    RoomPoiSyncResult syncFromPoi(double latitude, double longitude, double radiusKm);
}
