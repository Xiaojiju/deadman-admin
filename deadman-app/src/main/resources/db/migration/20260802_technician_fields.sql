-- =============================================================================
-- 技术人员附表字段重构：招聘类型/有无证/年限/吃住供应；移除旧技能与地点字段
-- 适用：已部署旧版 engineering_listing_technician 的 MySQL 8+ 环境
-- 注意：开发期破坏性变更，历史行用占位值回填，不做业务语义迁移
-- =============================================================================

SET @add_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_technician'
                 AND COLUMN_NAME = 'recruit_type'),
        'SELECT 1',
        'ALTER TABLE engineering_listing_technician
            ADD COLUMN recruit_type VARCHAR(64) NULL COMMENT ''招聘类型'' AFTER listing_id,
            ADD COLUMN has_certificate TINYINT NULL COMMENT ''有无证：0-无，1-有'' AFTER recruit_type,
            ADD COLUMN experience_years VARCHAR(64) NULL COMMENT ''年限'' AFTER has_certificate,
            ADD COLUMN accommodation VARCHAR(64) NULL COMMENT ''吃住供应'' AFTER experience_years')
);
PREPARE stmt FROM @add_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE engineering_listing_technician
SET recruit_type = COALESCE(NULLIF(TRIM(recruit_type), ''), '-'),
    has_certificate = COALESCE(has_certificate, 0),
    experience_years = COALESCE(NULLIF(TRIM(experience_years), ''), '-'),
    accommodation = COALESCE(NULLIF(TRIM(accommodation), ''), '-');

ALTER TABLE engineering_listing_technician
    MODIFY COLUMN recruit_type VARCHAR(64) NOT NULL COMMENT '招聘类型',
    MODIFY COLUMN has_certificate TINYINT NOT NULL COMMENT '有无证：0-无，1-有',
    MODIFY COLUMN experience_years VARCHAR(64) NOT NULL COMMENT '年限',
    MODIFY COLUMN accommodation VARCHAR(64) NOT NULL COMMENT '吃住供应';

SET @drop_old_cols := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'engineering_listing_technician'
                 AND COLUMN_NAME = 'skill_type'),
        'ALTER TABLE engineering_listing_technician
            DROP COLUMN skill_type,
            DROP COLUMN posting_location,
            DROP COLUMN vehicle_age,
            DROP COLUMN skill_certs,
            DROP COLUMN workplace_range',
        'SELECT 1')
);
PREPARE stmt FROM @drop_old_cols; EXECUTE stmt; DEALLOCATE PREPARE stmt;
