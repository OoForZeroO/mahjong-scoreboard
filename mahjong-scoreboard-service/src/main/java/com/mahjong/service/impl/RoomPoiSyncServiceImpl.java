package com.mahjong.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.dto.RoomPoiItem;
import com.mahjong.dto.RoomFromPoiRequest;
import com.mahjong.model.Room;
import com.mahjong.repository.RoomRepository;
import com.mahjong.service.RoomPoiSyncResult;
import com.mahjong.service.RoomPoiSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 从高德地图 POI 拉取「棋牌」相关地点并落库。
 * 高德周边搜索：https://restapi.amap.com/v5/place/around
 */
@Service
public class RoomPoiSyncServiceImpl implements RoomPoiSyncService {

    private static final Logger logger = LoggerFactory.getLogger(RoomPoiSyncServiceImpl.class);
    private static final String AMAP_AROUND_URL = "https://restapi.amap.com/v5/place/around";
    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 10;
    private static final int AMAP_RADIUS_METERS_MAX = 50000;

    @Value("${amap.api.key:}")
    private String amapApiKey;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<RoomPoiItem> searchFromPoi(double latitude, double longitude, double radiusKm) {
        if (amapApiKey == null || amapApiKey.isBlank()) {
            throw new IllegalStateException("未配置高德 API Key，请在配置文件中设置 amap.api.key");
        }
        int radiusM = (int) Math.min(radiusKm * 1000, AMAP_RADIUS_METERS_MAX);
        if (radiusM <= 0) radiusM = 5000;
        List<RoomPoiItem> result = new ArrayList<>();
        try {
            for (int pageNum = 1; pageNum <= MAX_PAGES; pageNum++) {
                String url = String.format("%s?key=%s&keywords=棋牌&location=%s,%s&radius=%d&page_size=%d&page_num=%d",
                        AMAP_AROUND_URL, amapApiKey, longitude, latitude, radiusM, PAGE_SIZE, pageNum);
                String body = restTemplate.getForObject(URI.create(url), String.class);
                if (body == null) break;
                JsonNode root = objectMapper.readTree(body);
                if (!"1".equals(root.path("status").asText(""))) {
                    if (pageNum == 1) throw new IllegalStateException("高德 API 返回: " + root.path("info").asText(""));
                    break;
                }
                JsonNode pois = root.path("pois");
                if (!pois.isArray() || pois.size() == 0) break;
                for (JsonNode poi : pois) {
                    RoomPoiItem item = parsePoiToItem(poi, latitude, longitude);
                    if (item != null) result.add(item);
                }
                if (pois.size() < PAGE_SIZE) break;
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("搜索高德 POI 失败", e);
            throw new RuntimeException("搜索失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Room saveFromPoi(RoomFromPoiRequest request) {
        if (request == null || request.getExternalId() == null || request.getExternalId().isBlank()) {
            throw new IllegalArgumentException("externalId 必填");
        }
        return roomRepository.findByExternalId(request.getExternalId())
                .orElseGet(() -> {
                    Room room = new Room();
                    room.setExternalId(request.getExternalId());
                    room.setName(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "棋牌室");
                    room.setLatitude(request.getLatitude());
                    room.setLongitude(request.getLongitude());
                    room.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
                    return roomRepository.save(room);
                });
    }

    private RoomPoiItem parsePoiToItem(JsonNode poi, double userLat, double userLng) {
        String externalId = poi.path("id").asText(null);
        if (externalId == null || externalId.isBlank()) return null;
        String name = poi.path("name").asText("").trim();
        if (name.isEmpty()) name = "棋牌室";
        String address = poi.path("address").asText(null);
        if (address != null) address = address.trim();
        String location = poi.path("location").asText("");
        double lat = 0, lng = 0;
        if (!location.isEmpty()) {
            String[] parts = location.split(",");
            if (parts.length >= 2) {
                try {
                    lng = Double.parseDouble(parts[0].trim());
                    lat = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        if (lat == 0 && lng == 0) return null;
        double distanceKm = haversineKm(userLat, userLng, lat, lng);
        distanceKm = Math.round(distanceKm * 100.0) / 100.0;
        return new RoomPoiItem(externalId, name, address, lat, lng, distanceKm);
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    @Override
    public RoomPoiSyncResult syncFromPoi(double latitude, double longitude, double radiusKm) {
        if (amapApiKey == null || amapApiKey.isBlank()) {
            return RoomPoiSyncResult.notConfigured("未配置高德 API Key，请在配置文件中设置 amap.api.key");
        }

        int radiusM = (int) Math.min(radiusKm * 1000, AMAP_RADIUS_METERS_MAX);
        if (radiusM <= 0) radiusM = 5000;

        int synced = 0;
        try {
            for (int pageNum = 1; pageNum <= MAX_PAGES; pageNum++) {
                String url = String.format("%s?key=%s&keywords=棋牌&location=%s,%s&radius=%d&page_size=%d&page_num=%d",
                        AMAP_AROUND_URL, amapApiKey, longitude, latitude, radiusM, PAGE_SIZE, pageNum);
                URI uri = URI.create(url);
                String body = restTemplate.getForObject(uri, String.class);
                if (body == null) break;

                JsonNode root = objectMapper.readTree(body);
                String status = root.path("status").asText("");
                if (!"1".equals(status)) {
                    String info = root.path("info").asText("");
                    logger.warn("高德 POI 返回异常: status={}, info={}", status, info);
                    if (pageNum == 1) return RoomPoiSyncResult.error("高德 API 返回: " + info);
                    break;
                }

                JsonNode pois = root.path("pois");
                if (!pois.isArray() || pois.size() == 0) break;

                for (JsonNode poi : pois) {
                    RoomPoiItem item = parsePoiToItem(poi, latitude, longitude);
                    if (item == null) continue;
                    if (roomRepository.findByExternalId(item.getExternalId()).isPresent()) continue;

                    Room room = new Room();
                    room.setName(item.getName());
                    room.setLatitude(item.getLatitude());
                    room.setLongitude(item.getLongitude());
                    room.setAddress(item.getAddress());
                    room.setExternalId(item.getExternalId());
                    roomRepository.save(room);
                    synced++;
                }
                if (pois.size() < PAGE_SIZE) break;
            }
            return RoomPoiSyncResult.ok(synced);
        } catch (Exception e) {
            logger.error("同步高德 POI 失败", e);
            return RoomPoiSyncResult.error(e.getMessage());
        }
    }
}
