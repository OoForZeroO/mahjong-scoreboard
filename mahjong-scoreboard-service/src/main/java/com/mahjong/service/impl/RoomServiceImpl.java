package com.mahjong.service.impl;

import com.mahjong.model.Room;
import com.mahjong.repository.RoomRepository;
import com.mahjong.service.RoomService;
import com.mahjong.dto.RoomNearbyItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room updateRoom(Long id, Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }

    @Override
    public List<RoomNearbyItem> findNearby(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null) {
            return List.of();
        }
        List<Room> withCoords = roomRepository.findByLatitudeIsNotNullAndLongitudeIsNotNull();
        return withCoords.stream()
                .map(r -> {
                    double d = haversineKm(latitude, longitude, r.getLatitude(), r.getLongitude());
                    return new RoomNearbyItem(r.getId(), r.getName(), r.getLogo(), Math.round(d * 100.0) / 100.0);
                })
                .filter(item -> radiusKm == null || radiusKm <= 0 || item.getDistanceKm() <= radiusKm)
                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
                .collect(Collectors.toList());
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}