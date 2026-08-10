-- =============================================================================
-- 无人机租赁附表字段重构：设备分类/载重/面议价格；移除名称型号成色旧字段
-- 适用：已部署旧版 engineering_listing_drone_rental 的 MySQL 8+ 环境
-- 注意：开发期破坏性变更，历史行用占位值回填，不做业务语义迁移
-- =============================================================================

SET @add_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_drone_rental'
                 AND COLUMN_NAME = 'dict_node_id'),
        'SELECT 1',
        'ALTER TABLE engineering_listing_drone_rental
            ADD COLUMN dict_node_id BIGINT NULL COMMENT ''设备分类字典节点 ID（三级）'' AFTER listing_id,
            ADD COLUMN load_capacity VARCHAR(64) NULL COMMENT ''载重'' AFTER brand_id,
            ADD COLUMN price DECIMAL(12, 2) NULL COMMENT ''参考价格（元，可空；业务上始终面议）'' AFTER load_capacity,
            ADD COLUMN price_negotiable TINYINT NULL COMMENT ''是否面议：0-否，1-是（固定 1）'' AFTER price')
);
PREPARE stmt FROM @add_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE engineering_listing_drone_rental
SET dict_node_id = COALESCE(dict_node_id, 0),
    load_capacity = COALESCE(NULLIF(TRIM(load_capacity), ''), '-'),
    price_negotiable = 1;

ALTER TABLE engineering_listing_drone_rental
    MODIFY COLUMN dict_node_id BIGINT NOT NULL COMMENT '设备分类字典节点 ID（三级）',
    MODIFY COLUMN load_capacity VARCHAR(64) NOT NULL COMMENT '载重',
    MODIFY COLUMN price DECIMAL(12, 2) NULL COMMENT '参考价格（元，可空；业务上始终面议）',
    MODIFY COLUMN price_negotiable TINYINT NOT NULL DEFAULT 1 COMMENT '是否面议：0-否，1-是（固定 1）';

SET @drop_old_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_drone_rental'
                 AND COLUMN_NAME = 'equipment_name'),
        'ALTER TABLE engineering_listing_drone_rental
            DROP COLUMN equipment_name,
            DROP COLUMN equipment_model,
            DROP COLUMN equipment_condition',
        'SELECT 1')
);
PREPARE stmt FROM @drop_old_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @add_dict_idx := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_drone_rental'
                 AND INDEX_NAME = 'idx_engineering_listing_drone_rental_dict_node'),
        'SELECT 1',
        'ALTER TABLE engineering_listing_drone_rental
            ADD KEY idx_engineering_listing_drone_rental_dict_node (dict_node_id)')
);
PREPARE stmt FROM @add_dict_idx; EXECUTE stmt; DEALLOCATE PREPARE stmt;
