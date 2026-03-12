package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 从互联网（高德 POI）搜索得到的门店项，供前端展示与选择，尚未落库。
 */
public class RoomPoiItem {

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    /** 与请求坐标的距离，公里 */
    @JsonProperty("distanceKm")
    private Double distanceKm;

    public RoomPoiItem() {}

    public RoomPoiItem(String externalId, String name, String address, Double latitude, Double longitude, Double distanceKm) {
        this.externalId = externalId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
    }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
