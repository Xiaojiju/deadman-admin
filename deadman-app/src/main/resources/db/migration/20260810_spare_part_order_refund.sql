-- 备件订单：累计退款金额
-- 已部署库增量执行；全新环境请直接使用 engineering/schema.sql

ALTER TABLE spare_part_order
    ADD COLUMN refunded_amount_cents INT NOT NULL DEFAULT 0 COMMENT '已成功退款累计金额（分）' AFTER total_amount_cents;
