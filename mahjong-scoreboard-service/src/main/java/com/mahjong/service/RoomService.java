package com.mahjong.service;

import com.mahjong.model.Room;
import com.mahjong.dto.RoomNearbyItem;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);
    Optional<Room> getRoomById(Long id);
    List<Room> getAllRooms();
    Room updateRoom(Long id, Room room);
    void deleteRoom(Long id);
    boolean existsByName(String name);

    /**
     * 按当前坐标搜索附近门店，按距离排序。
     * @param latitude 纬度
     * @param longitude 经度
     * @param radiusKm 半径（公里），null 或 <=0 表示不限制距离，仅按距离排序
     * @return 门店列表（含距离，按距离升序）
     */
    List<RoomNearbyItem> findNearby(Double latitude, Double longitude, Double radiusKm);
}