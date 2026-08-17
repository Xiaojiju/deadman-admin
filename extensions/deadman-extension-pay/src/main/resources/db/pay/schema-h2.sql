-- deadman-extension-pay 集成测试用 H2（MySQL 兼容模式）

CREATE TABLE IF NOT EXISTS plugin_pay_order (
    id                      BIGINT        NOT NULL,
    out_trade_no            VARCHAR(64)   NOT NULL,
    biz_order_no            VARCHAR(64)   NOT NULL,
    description             VARCHAR(256)  NOT NULL,
    amount_total            INT           NOT NULL,
    amount_refunded         INT           NOT NULL DEFAULT 0,
    status                  VARCHAR(32)   NOT NULL,
    pay_platform            VARCHAR(32)   NOT NULL,
    pay_method              VARCHAR(32)   NOT NULL,
    provider_id             VARCHAR(64)   NOT NULL,
    fund_lane               VARCHAR(32)   NOT NULL DEFAULT 'DIRECT',
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

CREATE TABLE IF NOT EXISTS plugin_pay_refund (
    id                      BIGINT        NOT NULL,
    out_refund_no           VARCHAR(64)   NOT NULL,
    out_trade_no            VARCHAR(64)   NOT NULL,
    biz_order_no            VARCHAR(64)   NOT NULL,
    amount_refund           INT           NOT NULL,
    amount_total            INT           NOT NULL,
    currency                VARCHAR(16)   NOT NULL DEFAULT 'CNY',
    status                  VARCHAR(32)   NOT NULL,
    pay_platform            VARCHAR(32)   NOT NULL,
    pay_method              VARCHAR(32)   NOT NULL,
    provider_id             VARCHAR(64)   NOT NULL,
    sub_mchid               VARCHAR(32),
    channel_refund_id       VARCHAR(64),
    channel_transaction_id  VARCHAR(64),
    reason                  VARCHAR(128),
    user_received_account   VARCHAR(128),
    notify_raw              CLOB,
    abnormal_handled        SMALLINT      NOT NULL DEFAULT 0,
    is_deleted              SMALLINT      NOT NULL DEFAULT 0,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_refund_no UNIQUE (out_refund_no)
);

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_quota (
    id                      BIGINT        NOT NULL,
    merchant_entity_type    VARCHAR(32)   NOT NULL,
    single_limit_cents      BIGINT        NOT NULL,
    user_daily_limit_cents  BIGINT        NOT NULL,
    daily_limit_cents       BIGINT        NOT NULL,
    monthly_limit_cents     BIGINT        NOT NULL,
    dispatch_enabled        SMALLINT      NOT NULL DEFAULT 1,
    is_deleted              SMALLINT      NOT NULL DEFAULT 0,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_batch (
    id                      BIGINT        NOT NULL,
    batch_no                VARCHAR(64)   NOT NULL,
    biz_order_no            VARCHAR(64)   NOT NULL,
    provider_id             VARCHAR(64)   NOT NULL,
    pay_platform            VARCHAR(32)   NOT NULL,
    openid                  VARCHAR(128)  NOT NULL,
    user_name               VARCHAR(512),
    amount_total_cents      BIGINT        NOT NULL,
    amount_success_cents    BIGINT        NOT NULL DEFAULT 0,
    transfer_scene_id       VARCHAR(36)   NOT NULL,
    transfer_remark         VARCHAR(64)   NOT NULL,
    status                  VARCHAR(32)   NOT NULL,
    bill_count              INT           NOT NULL DEFAULT 0,
    success_count           INT           NOT NULL DEFAULT 0,
    fail_count              INT           NOT NULL DEFAULT 0,
    pending_count           INT           NOT NULL DEFAULT 0,
    stop_reason             VARCHAR(256),
    is_deleted              SMALLINT      NOT NULL DEFAULT 0,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_transfer_batch_no UNIQUE (batch_no)
);

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_bill (
    id                      BIGINT        NOT NULL,
    out_bill_no             VARCHAR(64)   NOT NULL,
    batch_no                VARCHAR(64)   NOT NULL,
    biz_order_no            VARCHAR(64)   NOT NULL,
    seq_no                  INT           NOT NULL,
    provider_id             VARCHAR(64)   NOT NULL,
    pay_platform            VARCHAR(32)   NOT NULL,
    openid                  VARCHAR(128)  NOT NULL,
    user_name               VARCHAR(512),
    amount_cents            BIGINT        NOT NULL,
    transfer_scene_id       VARCHAR(36)   NOT NULL,
    transfer_remark         VARCHAR(64)   NOT NULL,
    status                  VARCHAR(32)   NOT NULL,
    channel_bill_no         VARCHAR(128),
    package_info            VARCHAR(1024),
    fail_reason             VARCHAR(256),
    notify_raw              CLOB,
    dispatched_time         TIMESTAMP,
    finished_time           TIMESTAMP,
    is_deleted              SMALLINT      NOT NULL DEFAULT 0,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_transfer_bill_no UNIQUE (out_bill_no)
);

INSERT INTO plugin_pay_transfer_quota (
    id, merchant_entity_type, single_limit_cents, user_daily_limit_cents,
    daily_limit_cents, monthly_limit_cents, dispatch_enabled, is_deleted, version
) SELECT 1, 'NON_INDIVIDUAL', 20000, 200000, 5000000, 3000000000, 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM plugin_pay_transfer_quota WHERE id = 1);

CREATE TABLE IF NOT EXISTS plugin_pay_basic_account_flow (
    id                      BIGINT        NOT NULL,
    flow_no                 VARCHAR(64)   NOT NULL,
    biz_scene               VARCHAR(64)   NOT NULL,
    direction               VARCHAR(8)    NOT NULL,
    amount_cents            BIGINT        NOT NULL,
    pay_platform            VARCHAR(32),
    biz_order_no            VARCHAR(64),
    channel_ref_no          VARCHAR(128),
    remark                  VARCHAR(512),
    idempotent_key          VARCHAR(128)  NOT NULL,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_basic_flow_no UNIQUE (flow_no),
    CONSTRAINT uk_plugin_pay_basic_idempotent UNIQUE (idempotent_key)
);

CREATE TABLE IF NOT EXISTS plugin_pay_operate_account_flow (
    id                      BIGINT        NOT NULL,
    flow_no                 VARCHAR(64)   NOT NULL,
    biz_scene               VARCHAR(64)   NOT NULL,
    direction               VARCHAR(8)    NOT NULL,
    amount_cents            BIGINT        NOT NULL,
    pay_platform            VARCHAR(32),
    biz_order_no            VARCHAR(64),
    channel_ref_no          VARCHAR(128),
    remark                  VARCHAR(512),
    idempotent_key          VARCHAR(128)  NOT NULL,
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_pay_operate_flow_no UNIQUE (flow_no),
    CONSTRAINT uk_plugin_pay_operate_idempotent UNIQUE (idempotent_key)
);
