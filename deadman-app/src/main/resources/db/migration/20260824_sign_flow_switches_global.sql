-- 签约流程全局开关：迁入 engineering_sign_fee_config（listing / 保险共用）
ALTER TABLE engineering_sign_fee_config
    ADD COLUMN manual_confirm_enabled TINYINT NOT NULL DEFAULT 1
        COMMENT '是否手动确认：1-需确认；0-建单自动确认' AFTER enabled,
    ADD COLUMN ess_enabled TINYINT NOT NULL DEFAULT 0
        COMMENT '是否走电子签：0-确认后完结；1-走支付与电子签' AFTER manual_confirm_enabled;

ALTER TABLE engineering_sign_order
    ADD COLUMN ess_enabled TINYINT NOT NULL DEFAULT 0
        COMMENT '是否走电子签（建单快照）' AFTER total_fee_cents;

-- 说明：若本地曾把 manual_confirm_enabled / ess_enabled 加在 engineering_insurance_sign_config，
-- 请手动 DROP 这两列；正式 schema 与保险建表迁移已不再包含它们。
