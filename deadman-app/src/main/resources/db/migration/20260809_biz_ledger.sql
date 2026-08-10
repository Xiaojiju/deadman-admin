-- 工程业务统一流水（会员 / 备件商品 / 信息）
-- 已部署库增量执行；全新环境请直接使用 engineering/schema.sql

CREATE TABLE IF NOT EXISTS engineering_biz_ledger (
    id                      BIGINT         NOT NULL COMMENT '主键',
    ledger_no               VARCHAR(64)    NOT NULL COMMENT '业务流水号（唯一）',
    biz_type                VARCHAR(32)    NOT NULL COMMENT '业务类型：membership/spare_part/listing',
    event_type              VARCHAR(32)    NOT NULL COMMENT '事件类型：paid/admin_grant/cancelled/refund',
    biz_order_id            BIGINT         NULL COMMENT '关联业务订单 ID',
    biz_order_no            VARCHAR(64)    NULL COMMENT '关联业务订单号',
    client_user_id          BIGINT         NOT NULL COMMENT '用户 ID（client 用户）',
    amount_cents            INT            NOT NULL DEFAULT 0 COMMENT '金额（分；后台开通可为 0）',
    event_time              DATETIME       NOT NULL COMMENT '事件发生时间（统计主时间）',
    plan_id                 BIGINT         NULL COMMENT '会员套餐 ID 快照',
    tier                    VARCHAR(32)    NULL COMMENT '会员等级编码快照',
    merchant_type           VARCHAR(64)    NULL COMMENT '商户类型编码快照（普通会员为空）',
    billing_cycle           VARCHAR(32)    NULL COMMENT '计费周期编码快照：monthly/quarterly/annual/custom',
    merchant_user_id        BIGINT         NULL COMMENT '备件卖家用户 ID',
    merchant_profile_id     BIGINT         NULL COMMENT '备件店铺资料 ID',
    listing_id              BIGINT         NULL COMMENT '信息 listing ID（预留）',
    idempotent_key          VARCHAR(128)   NOT NULL COMMENT '幂等键',
    remark                  VARCHAR(512)   NULL COMMENT '备注',
    create_time             DATETIME       NOT NULL COMMENT '创建时间',
    update_time             DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_engineering_biz_ledger_no (ledger_no),
    UNIQUE KEY uk_engineering_biz_ledger_idempotent (idempotent_key),
    KEY idx_biz_ledger_biz_event_time (biz_type, event_type, event_time),
    KEY idx_biz_ledger_mem_dims (biz_type, tier, merchant_type, billing_cycle, event_time),
    KEY idx_biz_ledger_client (client_user_id, event_time),
    KEY idx_biz_ledger_order_no (biz_order_no)
) COMMENT '工程业务统一流水（会员/备件/信息；非支付渠道账本）';
