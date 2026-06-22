-- deadman-plugin-pay 集成测试用 H2（MySQL 兼容模式）

CREATE TABLE IF NOT EXISTS plugin_pay_order (
    id                      BIGINT        NOT NULL,
    out_trade_no            VARCHAR(64)   NOT NULL,
    biz_order_no            VARCHAR(64)   NOT NULL,
    description             VARCHAR(256)  NOT NULL,
    amount_total            INT           NOT NULL,
    status                  VARCHAR(32)   NOT NULL,
    pay_platform            VARCHAR(32)   NOT NULL,
    pay_method              VARCHAR(32)   NOT NULL,
    provider_id             VARCHAR(64)   NOT NULL,
    channel_prepay_id       VARCHAR(128),
    channel_transaction_id  VARCHAR(64),
    channel_extra           VARCHAR(1024),
    payer_user_id           BIGINT,
    notify_raw              CLOB,
    is_deleted              SMALLINT      NOT NULL DEFAULT 0,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_out_trade_no UNIQUE (out_trade_no)
);
