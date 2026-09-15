-- 商户类型可发布能力可配置 + 保险产品归属商户
CREATE TABLE IF NOT EXISTS engineering_merchant_type_capability (
    id                BIGINT       NOT NULL COMMENT '主键',
    merchant_type     VARCHAR(32)  NOT NULL COMMENT '商户类型编码',
    capability_code   VARCHAR(64)  NOT NULL COMMENT '能力编码',
    create_time       DATETIME     NOT NULL COMMENT '创建时间',
    update_time       DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_eng_merchant_type_capability (merchant_type, capability_code),
    KEY idx_eng_merchant_type_capability_type (merchant_type)
) COMMENT '商户类型可发布/管理能力配置';

-- 默认灌入（与 MerchantType 枚举一致；若表已有数据可跳过重复）
INSERT IGNORE INTO engineering_merchant_type_capability (id, merchant_type, capability_code, create_time, update_time) VALUES
(1, 'premium_selected', 'machinery_rent', NOW(), NOW()),
(2, 'premium_selected', 'machinery_trade', NOW(), NOW()),
(3, 'premium_selected', 'legacy_attachment', NOW(), NOW()),
(4, 'premium_selected', 'insurance_consult', NOW(), NOW()),
(5, 'used_equipment', 'machinery_rent', NOW(), NOW()),
(6, 'used_equipment', 'machinery_trade', NOW(), NOW()),
(7, 'used_equipment', 'legacy_attachment', NOW(), NOW()),
(8, 'used_equipment', 'employee_management', NOW(), NOW()),
(9, 'spare_part', 'spare_part_product', NOW(), NOW()),
(10, 'spare_part', 'legacy_attachment', NOW(), NOW()),
(11, 'spare_part', 'employee_management', NOW(), NOW()),
(12, 'maintenance', 'maintenance_shop', NOW(), NOW()),
(13, 'transport', 'transport_info', NOW(), NOW()),
(14, 'drone', 'drone_rent_demand', NOW(), NOW());

ALTER TABLE engineering_insurance_product
    ADD COLUMN merchant_profile_id BIGINT NULL COMMENT '归属商户店铺 ID；空表示平台运营发布' AFTER id,
    ADD COLUMN publisher_client_user_id BIGINT NULL COMMENT '发布人用户 ID' AFTER merchant_profile_id,
    ADD KEY idx_eng_insurance_product_merchant (merchant_profile_id),
    ADD KEY idx_eng_insurance_product_publisher (publisher_client_user_id);
