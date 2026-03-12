package com.mahjong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 附近门店项，用于“按当前坐标搜索棋牌门店”下拉列表，含距离。
 */
public class RoomNearbyItem {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("logo")
    private String logo;

    /** 与当前坐标的距离，单位：公里 */
    @JsonProperty("distanceKm")
    private Double distanceKm;

    public RoomNearbyItem() {}

    public RoomNearbyItem(Long id, String name, String logo, Double distanceKm) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.distanceKm = distanceKm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
