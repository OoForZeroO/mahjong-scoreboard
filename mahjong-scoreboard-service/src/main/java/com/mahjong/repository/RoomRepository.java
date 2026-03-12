package com.mahjong.repository;

import com.mahjong.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);
    Optional<Room> findByName(String name);

    /** 查询所有已填写经纬度的门店（用于附近搜索） */
    List<Room> findByLatitudeIsNotNullAndLongitudeIsNotNull();
}