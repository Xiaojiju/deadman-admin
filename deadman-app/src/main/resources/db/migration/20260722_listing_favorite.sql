-- 用户收藏发布信息 / 商户店铺
CREATE TABLE IF NOT EXISTS client_listing_favorite (
    id              BIGINT        NOT NULL COMMENT '主键',
    client_user_id  BIGINT        NOT NULL COMMENT '用户 ID（client 用户）',
    listing_id      BIGINT        NOT NULL COMMENT '收藏的信息发布门面 ID',
    create_time     DATETIME      NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_listing_favorite_user_listing (client_user_id, listing_id),
    KEY idx_client_listing_favorite_user_time (client_user_id, create_time),
    KEY idx_client_listing_favorite_listing (listing_id)
) COMMENT '用户收藏的发布信息';

CREATE TABLE IF NOT EXISTS client_merchant_favorite (
    id                   BIGINT        NOT NULL COMMENT '主键',
    client_user_id       BIGINT        NOT NULL COMMENT '用户 ID（client 用户）',
    merchant_profile_id  BIGINT        NOT NULL COMMENT '商户店铺 ID',
    create_time          DATETIME      NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_merchant_favorite_user_profile (client_user_id, merchant_profile_id),
    KEY idx_client_merchant_favorite_user_time (client_user_id, create_time),
    KEY idx_client_merchant_favorite_profile (merchant_profile_id)
) COMMENT '用户收藏的商户店铺';
