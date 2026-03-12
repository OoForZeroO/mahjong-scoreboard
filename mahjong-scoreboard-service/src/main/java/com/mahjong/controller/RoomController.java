package com.mahjong.controller;

import com.mahjong.model.Room;
import com.mahjong.service.RoomService;
import com.mahjong.dto.RoomNearbyItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

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