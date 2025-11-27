package com.mahjong.service;

import com.mahjong.model.Room;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);
    Optional<Room> getRoomById(Long id);
    List<Room> getAllRooms();
    Room updateRoom(Long id, Room room);
    void deleteRoom(Long id);
    boolean existsByName(String name);
}