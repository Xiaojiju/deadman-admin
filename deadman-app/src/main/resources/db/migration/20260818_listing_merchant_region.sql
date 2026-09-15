-- 信息发布 / 商户店铺 / 入驻申请：结构化省市区（按市筛选）
ALTER TABLE engineering_listing
    ADD COLUMN province VARCHAR(64) NOT NULL DEFAULT '' COMMENT '省' AFTER address_text,
    ADD COLUMN city VARCHAR(64) NOT NULL DEFAULT '' COMMENT '市（直辖市存市名，便于按市筛选）' AFTER province,
    ADD COLUMN district VARCHAR(64) NOT NULL DEFAULT '' COMMENT '区/县' AFTER city,
    ADD KEY idx_engineering_listing_city (city, is_deleted);

ALTER TABLE merchant_profile
    ADD COLUMN province VARCHAR(64) NOT NULL DEFAULT '' COMMENT '省' AFTER address_text,
    ADD COLUMN city VARCHAR(64) NOT NULL DEFAULT '' COMMENT '市（直辖市存市名，便于按市筛选）' AFTER province,
    ADD COLUMN district VARCHAR(64) NOT NULL DEFAULT '' COMMENT '区/县' AFTER city,
    ADD KEY idx_merchant_profile_city (city, status);

ALTER TABLE merchant_application
    ADD COLUMN province VARCHAR(64) NOT NULL DEFAULT '' COMMENT '省' AFTER address_text,
    ADD COLUMN city VARCHAR(64) NOT NULL DEFAULT '' COMMENT '市（直辖市存市名，便于按市筛选）' AFTER province,
    ADD COLUMN district VARCHAR(64) NOT NULL DEFAULT '' COMMENT '区/县' AFTER city;
