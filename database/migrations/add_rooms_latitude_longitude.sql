-- 为门店表增加经纬度，用于“按当前坐标搜索附近棋牌门店”
-- PostgreSQL: 可使用 ADD COLUMN IF NOT EXISTS
-- MySQL: 若报错，请只保留下面两行 ALTER（无 IF NOT EXISTS）

ALTER TABLE rooms ADD COLUMN latitude DOUBLE PRECISION NULL;
ALTER TABLE rooms ADD COLUMN longitude DOUBLE PRECISION NULL;

-- 可选：为附近搜索建索引
-- CREATE INDEX idx_rooms_lat_lng ON rooms(latitude, longitude);
