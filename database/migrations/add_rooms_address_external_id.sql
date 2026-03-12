-- 门店表增加 address、external_id，用于从高德 POI 同步棋牌室信息并去重
-- PostgreSQL
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS address VARCHAR(500) NULL;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS external_id VARCHAR(64) NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_rooms_external_id ON rooms(external_id);

-- MySQL 若不支持 IF NOT EXISTS，可执行：
-- ALTER TABLE rooms ADD COLUMN address VARCHAR(500) NULL;
-- ALTER TABLE rooms ADD COLUMN external_id VARCHAR(64) NULL;
-- CREATE UNIQUE INDEX idx_rooms_external_id ON rooms(external_id);
