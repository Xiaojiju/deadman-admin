-- =============================================================================
-- 备件商品 SPU/SKU 多规格：价格库存下沉到 SKU；购物车按 sku_id
-- 适用：升级前已部署旧版备件表的 MySQL 8+ 环境（全新安装请直接执行 engineering/schema.sql）
-- 注意：本脚本会清空旧购物车，并删除旧 SPU 上的 price/stock 列（历史单价库存不再保留）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. SPU 附表：移除 price / stock（若仍存在）
-- -----------------------------------------------------------------------------
SET @drop_spare_part_price := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'engineering_listing_spare_part'
              AND COLUMN_NAME = 'price'
        ),
        'ALTER TABLE engineering_listing_spare_part DROP COLUMN price',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_spare_part_price;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_spare_part_stock := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'engineering_listing_spare_part'
              AND COLUMN_NAME = 'stock'
        ),
        'ALTER TABLE engineering_listing_spare_part DROP COLUMN stock',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_spare_part_stock;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE engineering_listing_spare_part
    MODIFY COLUMN image_urls VARCHAR(2048) NOT NULL COMMENT 'SPU 图片 URL 列表（JSON，最多 4 张）';

-- -----------------------------------------------------------------------------
-- 2. 规格 / SKU 新表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS engineering_listing_spare_part_spec (
    id              BIGINT       NOT NULL COMMENT '主键',
    spare_part_id   BIGINT       NOT NULL COMMENT '备件 SPU ID',
    spec_name       VARCHAR(64)  NOT NULL COMMENT '规格维度名称（如颜色、内存）',
    is_main         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否主规格：1-是，0-否（同一 SPU 有且仅有一个主规格）',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
    create_time     DATETIME     NOT NULL COMMENT '创建时间',
    update_time     DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_engineering_spare_part_spec_spu (spare_part_id, sort_order)
) COMMENT '备件商品规格维度';

CREATE TABLE IF NOT EXISTS engineering_listing_spare_part_spec_value (
    id              BIGINT         NOT NULL COMMENT '主键',
    spec_id         BIGINT         NOT NULL COMMENT '规格维度 ID',
    spare_part_id   BIGINT         NOT NULL COMMENT '备件 SPU ID（冗余，便于按 SPU 批量加载）',
    value_name      VARCHAR(64)    NOT NULL COMMENT '规格值名称（如黑色、128G）',
    image_urls      VARCHAR(2048)  NULL COMMENT '主规格值图片 URL 列表（JSON）；非主规格为 NULL',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
    create_time     DATETIME       NOT NULL COMMENT '创建时间',
    update_time     DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_engineering_spare_part_spec_value_spec (spec_id, sort_order),
    KEY idx_engineering_spare_part_spec_value_spu (spare_part_id)
) COMMENT '备件商品规格值';

CREATE TABLE IF NOT EXISTS engineering_listing_spare_part_sku (
    id              BIGINT         NOT NULL COMMENT '主键',
    spare_part_id   BIGINT         NOT NULL COMMENT '备件 SPU ID',
    listing_id      BIGINT         NOT NULL COMMENT '信息发布 ID（冗余）',
    price           DECIMAL(12, 2) NOT NULL COMMENT '单价（元）',
    stock           INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
    create_time     DATETIME       NOT NULL COMMENT '创建时间',
    update_time     DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_engineering_spare_part_sku_spu (spare_part_id, sort_order),
    KEY idx_engineering_spare_part_sku_listing (listing_id)
) COMMENT '备件商品 SKU';

CREATE TABLE IF NOT EXISTS engineering_listing_spare_part_sku_value (
    id              BIGINT    NOT NULL COMMENT '主键',
    sku_id          BIGINT    NOT NULL COMMENT 'SKU ID',
    spare_part_id   BIGINT    NOT NULL COMMENT '备件 SPU ID（冗余）',
    spec_id         BIGINT    NOT NULL COMMENT '规格维度 ID',
    spec_value_id   BIGINT    NOT NULL COMMENT '规格值 ID',
    create_time     DATETIME  NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_engineering_spare_part_sku_spec (sku_id, spec_id),
    KEY idx_engineering_spare_part_sku_value_sku (sku_id),
    KEY idx_engineering_spare_part_sku_value_spu (spare_part_id)
) COMMENT '备件 SKU 与规格值关联';

-- -----------------------------------------------------------------------------
-- 3. 购物车改绑 sku_id（旧数据无法映射，直接清空）
-- -----------------------------------------------------------------------------
DELETE FROM client_spare_part_cart_item;

SET @has_cart_sku := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'client_spare_part_cart_item'
      AND COLUMN_NAME = 'sku_id'
);
SET @add_cart_sku := IF(
    @has_cart_sku = 0,
    'ALTER TABLE client_spare_part_cart_item ADD COLUMN sku_id BIGINT NOT NULL COMMENT ''备件 SKU ID'' AFTER listing_id',
    'SELECT 1'
);
PREPARE stmt FROM @add_cart_sku;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_cart_uk_listing := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'client_spare_part_cart_item'
              AND INDEX_NAME = 'uk_client_spare_part_cart_user_listing'
        ),
        'ALTER TABLE client_spare_part_cart_item DROP INDEX uk_client_spare_part_cart_user_listing',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_cart_uk_listing;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_cart_uk_sku := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'client_spare_part_cart_item'
              AND INDEX_NAME = 'uk_client_spare_part_cart_user_sku'
        ),
        'SELECT 1',
        'ALTER TABLE client_spare_part_cart_item ADD UNIQUE KEY uk_client_spare_part_cart_user_sku (client_user_id, sku_id)'
    )
);
PREPARE stmt FROM @add_cart_uk_sku;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_cart_listing_idx := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'client_spare_part_cart_item'
              AND INDEX_NAME = 'idx_client_spare_part_cart_listing'
        ),
        'SELECT 1',
        'ALTER TABLE client_spare_part_cart_item ADD KEY idx_client_spare_part_cart_listing (listing_id)'
    )
);
PREPARE stmt FROM @add_cart_listing_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
