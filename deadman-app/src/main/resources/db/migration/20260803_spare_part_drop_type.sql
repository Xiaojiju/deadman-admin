-- =============================================================================
-- 备件 SPU 去掉 spare_part_type：类型与字典分类 dict_node_id 为同一概念
-- 适用：已部署含 spare_part_type 的 MySQL 8+ 环境
-- =============================================================================

SET @drop_col := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_spare_part'
                 AND COLUMN_NAME = 'spare_part_type'),
        'ALTER TABLE engineering_listing_spare_part DROP COLUMN spare_part_type',
        'SELECT 1')
);
PREPARE stmt FROM @drop_col; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE engineering_listing_spare_part
    MODIFY COLUMN dict_node_id BIGINT NOT NULL COMMENT '备件分类字典节点 ID（即备件类型）';
