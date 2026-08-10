-- 评价体系：商户评价 + 备件商品评价

CREATE TABLE IF NOT EXISTS merchant_review (
    id                      BIGINT         NOT NULL COMMENT '主键',
    client_user_id          BIGINT         NOT NULL COMMENT '评价用户 ID（client 用户）',
    merchant_user_id        BIGINT         NOT NULL COMMENT '被评商户所有者用户 ID',
    merchant_profile_id     BIGINT         NOT NULL COMMENT '被评店铺资料 ID',
    merchant_name           VARCHAR(128)   NULL COMMENT '店铺名称快照',
    star                    TINYINT        NOT NULL COMMENT '星级：1-5',
    content                 VARCHAR(1000)  NULL COMMENT '文字评价',
    image_urls              VARCHAR(2048)  NULL COMMENT '评价图片 URL 列表（JSON 数组）',
    status                  TINYINT        NOT NULL DEFAULT 1 COMMENT '展示状态：0-隐藏，1-展示',
    create_time             DATETIME       NOT NULL COMMENT '创建时间',
    update_time             DATETIME       NOT NULL COMMENT '更新时间',
    is_deleted              TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    version                 INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_review_user_profile (client_user_id, merchant_profile_id),
    KEY idx_merchant_review_profile_status (merchant_profile_id, status, create_time, is_deleted),
    KEY idx_merchant_review_merchant_user (merchant_user_id, create_time, is_deleted)
) COMMENT '商户（店铺）评价';

CREATE TABLE IF NOT EXISTS spare_part_product_review (
    id                      BIGINT         NOT NULL COMMENT '主键',
    client_user_id          BIGINT         NOT NULL COMMENT '评价用户 ID（client 用户）',
    order_id                BIGINT         NOT NULL COMMENT '关联订单主表 ID',
    order_no                VARCHAR(64)    NOT NULL COMMENT '业务订单号',
    order_item_id           BIGINT         NOT NULL COMMENT '关联订单明细 ID（一明细一评）',
    source_listing_id       BIGINT         NULL COMMENT '原商品 listing ID（仅追溯/聚合）',
    source_sku_id           BIGINT         NULL COMMENT '原 SKU ID（仅追溯）',
    product_title           VARCHAR(128)   NULL COMMENT '商品标题快照',
    cover_image_url         VARCHAR(512)   NULL COMMENT '商品封面图快照',
    merchant_user_id        BIGINT         NOT NULL COMMENT '卖家用户 ID',
    merchant_profile_id     BIGINT         NULL COMMENT '店铺资料 ID 快照',
    star                    TINYINT        NOT NULL COMMENT '星级：1-5',
    content                 VARCHAR(1000)  NULL COMMENT '文字评价',
    image_urls              VARCHAR(2048)  NULL COMMENT '评价图片 URL 列表（JSON 数组）',
    status                  TINYINT        NOT NULL DEFAULT 1 COMMENT '展示状态：0-隐藏，1-展示',
    create_time             DATETIME       NOT NULL COMMENT '创建时间',
    update_time             DATETIME       NOT NULL COMMENT '更新时间',
    is_deleted              TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    version                 INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_spare_part_product_review_item (order_item_id),
    KEY idx_product_review_listing_status (source_listing_id, status, create_time, is_deleted),
    KEY idx_product_review_order (order_no, client_user_id, is_deleted),
    KEY idx_product_review_merchant (merchant_user_id, create_time, is_deleted)
) COMMENT '备件商品评价（确认收货后按订单明细评价）';
