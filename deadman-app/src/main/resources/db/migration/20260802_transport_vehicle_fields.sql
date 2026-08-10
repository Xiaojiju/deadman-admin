-- =============================================================================
-- 物流运输附表字段重构：年限/环保/载重/公里单价/目的地；车类沿用 dict_node_id
-- 适用：已部署旧版 engineering_listing_transport_vehicle 的 MySQL 8+ 环境
-- 注意：开发期破坏性变更，历史行用占位值回填后强制 NOT NULL，不做业务语义迁移
-- =============================================================================

SET @add_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_transport_vehicle'
                 AND COLUMN_NAME = 'equipment_age'),
        'SELECT 1',
        'ALTER TABLE engineering_listing_transport_vehicle
            ADD COLUMN equipment_age VARCHAR(64) NULL COMMENT ''年限'' AFTER dict_node_id,
            ADD COLUMN environmental_standard VARCHAR(64) NULL COMMENT ''环保标准'' AFTER equipment_age,
            ADD COLUMN load_capacity VARCHAR(64) NULL COMMENT ''载重'' AFTER environmental_standard,
            ADD COLUMN price_per_km DECIMAL(12, 2) NULL COMMENT ''公里单价（元）'' AFTER load_capacity,
            ADD COLUMN destination_address_text VARCHAR(512) NULL COMMENT ''目的地'' AFTER price_per_km,
            ADD COLUMN destination_latitude DECIMAL(10, 6) NULL COMMENT ''目的地纬度'' AFTER destination_address_text,
            ADD COLUMN destination_longitude DECIMAL(10, 6) NULL COMMENT ''目的地经度'' AFTER destination_latitude')
);
PREPARE stmt FROM @add_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE engineering_listing_transport_vehicle
SET equipment_age = COALESCE(NULLIF(TRIM(equipment_age), ''), '-'),
    environmental_standard = COALESCE(NULLIF(TRIM(environmental_standard), ''), '-'),
    load_capacity = COALESCE(NULLIF(TRIM(load_capacity), ''), '-'),
    price_per_km = COALESCE(price_per_km, 0.01),
    destination_address_text = COALESCE(NULLIF(TRIM(destination_address_text), ''), '-'),
    destination_latitude = COALESCE(destination_latitude, 0),
    destination_longitude = COALESCE(destination_longitude, 0);

ALTER TABLE engineering_listing_transport_vehicle
    MODIFY COLUMN dict_node_id BIGINT NOT NULL COMMENT '车类字典节点 ID（三级）',
    MODIFY COLUMN equipment_age VARCHAR(64) NOT NULL COMMENT '年限',
    MODIFY COLUMN environmental_standard VARCHAR(64) NOT NULL COMMENT '环保标准',
    MODIFY COLUMN load_capacity VARCHAR(64) NOT NULL COMMENT '载重',
    MODIFY COLUMN price_per_km DECIMAL(12, 2) NOT NULL COMMENT '公里单价（元）',
    MODIFY COLUMN destination_address_text VARCHAR(512) NOT NULL COMMENT '目的地',
    MODIFY COLUMN destination_latitude DECIMAL(10, 6) NOT NULL COMMENT '目的地纬度',
    MODIFY COLUMN destination_longitude DECIMAL(10, 6) NOT NULL COMMENT '目的地经度';

SET @drop_vehicle_category := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_transport_vehicle'
                 AND COLUMN_NAME = 'vehicle_category'),
        'ALTER TABLE engineering_listing_transport_vehicle DROP COLUMN vehicle_category',
        'SELECT 1')
);
PREPARE stmt FROM @drop_vehicle_category; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_brand_idx := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_transport_vehicle'
                 AND INDEX_NAME = 'idx_engineering_listing_transport_vehicle_brand'),
        'ALTER TABLE engineering_listing_transport_vehicle DROP INDEX idx_engineering_listing_transport_vehicle_brand',
        'SELECT 1')
);
PREPARE stmt FROM @drop_brand_idx; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_old_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_transport_vehicle'
                 AND COLUMN_NAME = 'brand_id'),
        'ALTER TABLE engineering_listing_transport_vehicle
            DROP COLUMN brand_id,
            DROP COLUMN equipment_model,
            DROP COLUMN quantity,
            DROP COLUMN unit_price,
            DROP COLUMN operating_range,
            DROP COLUMN freight,
            DROP COLUMN transport_route',
        'SELECT 1')
);
PREPARE stmt FROM @drop_old_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;
