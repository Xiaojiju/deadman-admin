-- 商家转账：额度配置、转账批次、转账明细（待转拆单）

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_quota (
    id                      BIGINT        NOT NULL COMMENT '主键',
    merchant_entity_type    VARCHAR(32)   NOT NULL COMMENT '商户主体：NON_INDIVIDUAL/INDIVIDUAL',
    single_limit_cents      BIGINT        NOT NULL COMMENT '单笔限额（分）',
    user_daily_limit_cents  BIGINT        NOT NULL COMMENT '单用户单日限额（分）',
    daily_limit_cents       BIGINT        NOT NULL COMMENT '单日总额度（分）',
    monthly_limit_cents     BIGINT        NOT NULL COMMENT '单月总额度（分，不可调）',
    dispatch_enabled        SMALLINT      NOT NULL DEFAULT 1 COMMENT '是否允许自动/手动派发待转：1-是，0-人工停止',
    is_deleted              SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_pay_transfer_quota (merchant_entity_type, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家转账额度配置';

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_batch (
    id                      BIGINT        NOT NULL COMMENT '主键',
    batch_no                VARCHAR(64)   NOT NULL COMMENT '平台转账批次号',
    biz_order_no            VARCHAR(64)   NOT NULL COMMENT '业务单号',
    provider_id             VARCHAR(64)   NOT NULL COMMENT 'Transfer Provider 标识',
    pay_platform            VARCHAR(32)   NOT NULL COMMENT '支付平台',
    openid                  VARCHAR(128)  NOT NULL COMMENT '收款用户 openid',
    user_name               VARCHAR(512)           COMMENT '收款用户姓名（密文 token 或明文兼容）',
    amount_total_cents      BIGINT        NOT NULL COMMENT '批次总金额（分）',
    amount_success_cents    BIGINT        NOT NULL DEFAULT 0 COMMENT '已成功金额（分）',
    transfer_scene_id       VARCHAR(36)   NOT NULL COMMENT '转账场景 ID',
    transfer_remark         VARCHAR(64)   NOT NULL COMMENT '转账备注',
    status                  VARCHAR(32)   NOT NULL COMMENT '批次状态：PENDING/DISPATCHING/PARTIAL_SUCCESS/SUCCESS/FAILED/STOPPED',
    bill_count              INT           NOT NULL DEFAULT 0 COMMENT '拆单笔数',
    success_count           INT           NOT NULL DEFAULT 0 COMMENT '成功笔数',
    fail_count              INT           NOT NULL DEFAULT 0 COMMENT '失败笔数',
    pending_count           INT           NOT NULL DEFAULT 0 COMMENT '待转笔数',
    stop_reason             VARCHAR(256)           COMMENT '停止原因',
    is_deleted              SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_pay_transfer_batch_no (batch_no),
    UNIQUE KEY uk_plugin_pay_transfer_batch_biz (biz_order_no, is_deleted),
    KEY idx_plugin_pay_transfer_batch_status (status, create_time, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家转账批次';

CREATE TABLE IF NOT EXISTS plugin_pay_transfer_bill (
    id                      BIGINT        NOT NULL COMMENT '主键',
    out_bill_no             VARCHAR(64)   NOT NULL COMMENT '平台转账单号（渠道 out_bill_no）',
    batch_no                VARCHAR(64)   NOT NULL COMMENT '所属批次号',
    biz_order_no            VARCHAR(64)   NOT NULL COMMENT '业务单号',
    seq_no                  INT           NOT NULL COMMENT '批次内序号（从 1 开始）',
    provider_id             VARCHAR(64)   NOT NULL COMMENT 'Provider 标识',
    pay_platform            VARCHAR(32)   NOT NULL COMMENT '支付平台',
    openid                  VARCHAR(128)  NOT NULL COMMENT '收款用户 openid',
    user_name               VARCHAR(512)           COMMENT '收款用户姓名',
    amount_cents            BIGINT        NOT NULL COMMENT '本笔金额（分）',
    transfer_scene_id       VARCHAR(36)   NOT NULL COMMENT '转账场景 ID',
    transfer_remark         VARCHAR(64)   NOT NULL COMMENT '转账备注',
    status                  VARCHAR(32)   NOT NULL COMMENT 'PENDING/ACCEPTED/PROCESSING/WAIT_USER_CONFIRM/TRANSFERING/SUCCESS/FAIL/CANCELLED',
    channel_bill_no         VARCHAR(128)           COMMENT '渠道转账单号',
    package_info            VARCHAR(1024)          COMMENT '用户确认收款 package',
    fail_reason             VARCHAR(256)           COMMENT '失败原因',
    notify_raw              TEXT                   COMMENT '最近回调/查单原文',
    dispatched_time         TIMESTAMP              NULL COMMENT '实际发起渠道时间',
    finished_time           TIMESTAMP              NULL COMMENT '终态时间',
    is_deleted              SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_pay_transfer_bill_no (out_bill_no),
    KEY idx_plugin_pay_transfer_bill_batch (batch_no, seq_no, is_deleted),
    KEY idx_plugin_pay_transfer_bill_status (status, create_time, is_deleted),
    KEY idx_plugin_pay_transfer_bill_openid_day (openid, status, create_time, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家转账明细（拆单待转）';

-- 默认额度：非个体户（可按 API 调整单笔/单用户日/单日）
INSERT INTO plugin_pay_transfer_quota (
    id, merchant_entity_type, single_limit_cents, user_daily_limit_cents,
    daily_limit_cents, monthly_limit_cents, dispatch_enabled, is_deleted, version
) VALUES (
    1, 'NON_INDIVIDUAL', 20000, 200000, 5000000, 3000000000, 1, 0, 0
) ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;
