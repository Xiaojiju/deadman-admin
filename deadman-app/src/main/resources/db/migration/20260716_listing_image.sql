-- =============================================================================
-- 信息发布统一图片关联表；各 listing 附表移除 image_urls JSON 列
-- 适用：已部署旧版含 image_urls 的 MySQL 8+ 环境（全新安装请直接执行 engineering/schema.sql）
-- 注意：开发期破坏性变更，不做历史 JSON 回填
-- =============================================================================

CREATE TABLE IF NOT EXISTS engineering_listing_image (
    id              BIGINT       NOT NULL COMMENT '主键',
    owner_type      VARCHAR(64)  NOT NULL COMMENT '归属类型：listing / spare_part_spec_value',
    owner_id        BIGINT       NOT NULL COMMENT '归属实体主键（listingId 或 specValueId）',
    file_id         BIGINT       NOT NULL COMMENT 'plugin_file_metadata.id',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
    is_deleted      SMALLINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     DATETIME     NOT NULL COMMENT '创建时间',
    update_time     DATETIME     NOT NULL COMMENT '更新时间',
    version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_eng_listing_image_owner (owner_type, owner_id, sort_order, is_deleted),
    KEY idx_eng_listing_image_file (file_id, is_deleted)
) COMMENT '信息发布统一图片关联（fileId）';

-- 安全删除各附表 image_urls（列不存在则跳过）
SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_machinery_rental' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_machinery_rental DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_legacy_attachment' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_legacy_attachment DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_maintenance' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_maintenance DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_transport_vehicle' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_transport_vehicle DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_drone_rental' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_drone_rental DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_spare_part' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_spare_part DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'engineering_listing_spare_part_spec_value' AND COLUMN_NAME = 'image_urls'),
        'ALTER TABLE engineering_listing_spare_part_spec_value DROP COLUMN image_urls',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;
