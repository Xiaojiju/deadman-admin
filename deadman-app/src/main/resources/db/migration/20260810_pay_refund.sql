-- 支付退款：支付单累计退款金额 + 退款单表
-- 已部署库增量执行；全新环境请直接使用 extensions/deadman-extension-pay/.../db/pay/schema.sql

ALTER TABLE plugin_pay_order
    ADD COLUMN amount_refunded INT NOT NULL DEFAULT 0 COMMENT '已成功退款累计金额（分）' AFTER amount_total;

CREATE TABLE IF NOT EXISTS plugin_pay_refund (
    id                      BIGINT        NOT NULL COMMENT '主键',
    out_refund_no           VARCHAR(64)   NOT NULL COMMENT '平台退款单号（商户侧 out_refund_no）',
    out_trade_no            VARCHAR(64)   NOT NULL COMMENT '平台支付单号',
    biz_order_no            VARCHAR(64)   NOT NULL COMMENT '业务订单号',
    amount_refund           INT           NOT NULL COMMENT '本次退款金额（分）',
    amount_total            INT           NOT NULL COMMENT '原支付订单总金额（分）',
    currency                VARCHAR(16)   NOT NULL DEFAULT 'CNY' COMMENT '退款币种',
    status                  VARCHAR(32)   NOT NULL COMMENT '退款状态：PROCESSING/SUCCESS/CLOSED/ABNORMAL',
    pay_platform            VARCHAR(32)   NOT NULL COMMENT '支付平台',
    pay_method              VARCHAR(32)   NOT NULL COMMENT '支付方式',
    provider_id             VARCHAR(64)   NOT NULL COMMENT 'Provider 标识',
    channel_refund_id       VARCHAR(64)            COMMENT '渠道退款单号',
    channel_transaction_id  VARCHAR(64)            COMMENT '渠道支付单号',
    reason                  VARCHAR(128)           COMMENT '退款原因',
    user_received_account   VARCHAR(128)           COMMENT '退款入账账户描述',
    notify_raw              TEXT                   COMMENT '最近一次回调/查单原文',
    abnormal_handled        SMALLINT      NOT NULL DEFAULT 0 COMMENT '是否已发起过异常退款：0-否，1-是',
    is_deleted              SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_pay_refund_no (out_refund_no),
    KEY idx_plugin_pay_refund_trade (out_trade_no, status, is_deleted),
    KEY idx_plugin_pay_refund_biz (biz_order_no, is_deleted),
    KEY idx_plugin_pay_refund_status_create (status, create_time, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付退款单';
