-- deadman-extension-pay 统一支付单表（MySQL 8+）

CREATE TABLE IF NOT EXISTS plugin_pay_order (
    id                      BIGINT        NOT NULL COMMENT '主键',
    out_trade_no            VARCHAR(64)   NOT NULL COMMENT '平台支付单号（商户侧 out_trade_no）',
    biz_order_no            VARCHAR(64)   NOT NULL COMMENT '业务订单号',
    description             VARCHAR(256)  NOT NULL COMMENT '商品描述',
    amount_total            INT           NOT NULL COMMENT '订单金额（分）',
    status                  VARCHAR(32)   NOT NULL COMMENT '支付状态：NOT_PAY/SUCCESS/CLOSED/REFUND',
    pay_platform            VARCHAR(32)   NOT NULL COMMENT '支付平台：WECHAT/ALIPAY 等',
    pay_method              VARCHAR(32)   NOT NULL COMMENT '支付方式：JSAPI/NATIVE/APP/H5 等',
    provider_id             VARCHAR(64)   NOT NULL COMMENT '支付 Provider 标识',
    channel_prepay_id       VARCHAR(128)           COMMENT '渠道预支付 ID',
    channel_transaction_id  VARCHAR(64)            COMMENT '渠道支付单号',
    channel_extra           VARCHAR(1024)          COMMENT '渠道扩展信息 JSON',
    payer_user_id           BIGINT                 COMMENT '付款人用户 ID',
    notify_raw              TEXT                   COMMENT '最近一次回调原文',
    is_deleted              SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_pay_out_trade_no (out_trade_no),
    KEY idx_plugin_pay_biz_order (biz_order_no, is_deleted),
    KEY idx_plugin_pay_payer_user (payer_user_id, status, is_deleted),
    KEY idx_plugin_pay_platform_method (pay_platform, pay_method, status, is_deleted),
    KEY idx_plugin_pay_status_create (status, create_time, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付平台单';
