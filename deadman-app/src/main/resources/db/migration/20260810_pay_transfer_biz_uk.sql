-- 升级用：若已执行旧版 20260810_pay_transfer.sql（biz 为普通索引），请执行本脚本改为唯一索引。
-- 若新建库已直接带 uk_plugin_pay_transfer_batch_biz，可跳过本脚本。

ALTER TABLE plugin_pay_transfer_batch
    DROP INDEX idx_plugin_pay_transfer_batch_biz;

ALTER TABLE plugin_pay_transfer_batch
    ADD UNIQUE KEY uk_plugin_pay_transfer_batch_biz (biz_order_no, is_deleted);
