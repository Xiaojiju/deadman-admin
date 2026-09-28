-- 管理端纠纷：登记、核查时间轴、调解完成

-- 管理端登记的纠纷。投诉人和被投诉人分表保存，双方都可以有多人。
CREATE TABLE IF NOT EXISTS wgb_dispute (
    id             BIGINT        NOT NULL COMMENT '纠纷主键',
    reason         VARCHAR(1000) NOT NULL COMMENT '纠纷原因',
    occurred_at    TIMESTAMP     NOT NULL COMMENT '发生时间',
    status         VARCHAR(32)   NOT NULL COMMENT 'REGISTERED-已登记，CHECKING-核查中，MEDIATED-已调解',
    solution       VARCHAR(2000)          COMMENT '调解解决方案',
    registered_by  BIGINT        NOT NULL COMMENT '登记人，管理端用户 ID',
    mediated_by    BIGINT                 COMMENT '确认调解完成的管理员',
    mediated_at    TIMESTAMP              COMMENT '调解完成时间',
    is_deleted     SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
    update_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_wgb_dispute_status (status, create_time),
    KEY idx_wgb_dispute_occurred (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纠纷';

-- 纠纷当事人。同一方内按登记时的选择顺序保存。
CREATE TABLE IF NOT EXISTS wgb_dispute_party (
    id            BIGINT       NOT NULL COMMENT '主键',
    dispute_id    BIGINT       NOT NULL COMMENT '纠纷主键',
    user_id       BIGINT       NOT NULL COMMENT '用户端用户 ID',
    side          VARCHAR(16)  NOT NULL COMMENT 'COMPLAINANT-投诉人，RESPONDENT-被投诉人',
    display_name  VARCHAR(64)  NOT NULL COMMENT '登记时的展示名',
    sort_order    INT          NOT NULL COMMENT '同一方内的选择顺序，从 0 开始',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wgb_dispute_party (dispute_id, side, user_id),
    KEY idx_wgb_dispute_party_user (user_id, dispute_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纠纷当事人';

-- 纠纷核查记录。同一纠纷可多次填写，按时间排列成时间轴。
CREATE TABLE IF NOT EXISTS wgb_dispute_check (
    id             BIGINT        NOT NULL COMMENT '主键',
    dispute_id     BIGINT        NOT NULL COMMENT '纠纷主键',
    content        VARCHAR(2000) NOT NULL COMMENT '核查内容',
    admin_user_id  BIGINT        NOT NULL COMMENT '填写人，管理端用户 ID',
    create_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '填写时间',
    PRIMARY KEY (id),
    KEY idx_wgb_dispute_check (dispute_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纠纷核查记录';
