package com.mahjong.controller;

import com.mahjong.model.Room;
import com.mahjong.service.RoomService;
import com.mahjong.service.RoomPoiSyncService;
import com.mahjong.service.RoomPoiSyncResult;
import com.mahjong.dto.RoomNearbyItem;
import com.mahjong.dto.RoomPoiItem;
import com.mahjong.dto.RoomFromPoiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomPoiSyncService roomPoiSyncService;

    // 创建棋牌室
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room createdRoom = roomService.createRoom(room);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    // 获取所有棋牌室
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 按当前坐标搜索附近门店，用于输入框下拉选择。
     * 参数：latitude 纬度（必填）, longitude 经度（必填）, radius 半径公里（可选，不传则不限制距离，仅按距离排序）
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RoomNearbyItem>> getNearbyRooms(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radius) {
        List<RoomNearbyItem> list = roomService.findNearby(latitude, longitude, radius);
        return ResponseEntity.ok(list);
    }

    /**
     * 从互联网（高德 POI）搜索「棋牌」相关地点，仅返回列表不落库。
     * 前端用此接口展示下拉列表，用户选择后再调用 save-from-poi 落库。
     * 参数：latitude 纬度, longitude 经度, radiusKm 半径公里（可选，默认 10）
     */
    @GetMapping("/search-from-poi")
    public ResponseEntity<?> searchFromPoi(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {
        try {
            List<RoomPoiItem> list = roomPoiSyncService.searchFromPoi(latitude, longitude, radiusKm);
            return ResponseEntity.ok(list);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 将前端选中的 POI 门店落库。若 externalId 已存在则返回已存在记录。
     * Body: { "externalId", "name", "latitude", "longitude", "address"? }
     */
    @PostMapping("/save-from-poi")
    public ResponseEntity<Room> saveFromPoi(@RequestBody RoomFromPoiRequest request) {
        Room room = roomPoiSyncService.saveFromPoi(request);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    /**
     * 从高德 POI 拉取「棋牌」相关地点并落库。
     * 需配置 amap.api.key。参数：latitude 纬度, longitude 经度, radiusKm 半径（公里，可选，默认 10）
     */
    @PostMapping("/sync-from-poi")
    public ResponseEntity<Map<String, Object>> syncFromPoi(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {
        RoomPoiSyncResult result = roomPoiSyncService.syncFromPoi(latitude, longitude, radiusKm);
        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of("synced", result.getSynced(), "message", result.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("synced", 0, "message", result.getMessage()));
    }

    // 根据ID获取棋牌室
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 更新棋牌室
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        Room updatedRoom = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updatedRoom);
    }

    // 删除棋牌室
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // 检查棋牌室名称是否存在
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        return ResponseEntity.ok(roomService.existsByName(name));
    }
}