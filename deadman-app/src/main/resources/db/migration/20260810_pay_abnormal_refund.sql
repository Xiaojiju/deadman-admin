-- 退款单：异常退款已处理标记
-- 若已执行含 abnormal_handled 的 20260810_pay_refund.sql 可跳过

ALTER TABLE plugin_pay_refund
    ADD COLUMN abnormal_handled SMALLINT NOT NULL DEFAULT 0 COMMENT '是否已发起过异常退款：0-否，1-是' AFTER notify_raw;
