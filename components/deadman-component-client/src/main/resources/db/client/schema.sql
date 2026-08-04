-- deadman-component-client 持久化表结构（MySQL 8+）
-- 与管理系统 user_base / user_account / user_password 完全隔离

-- 用户端用户基础信息
CREATE TABLE IF NOT EXISTS client_user_base (
    id              BIGINT       NOT NULL COMMENT '用户主键，雪花算法生成',
    user_code       VARCHAR(32)  NOT NULL COMMENT '对外用户编码',
    nickname        VARCHAR(64)           COMMENT '用户昵称',
    avatar          VARCHAR(512)          COMMENT '头像 URL',
    status          SMALLINT     NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    is_deleted      SMALLINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_user_base_user_code (user_code),
    KEY idx_client_user_base_status (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户端用户基础信息';

-- 用户端登录账号
CREATE TABLE IF NOT EXISTS client_user_account (
    id                  BIGINT        NOT NULL COMMENT '账号记录主键',
    user_id             BIGINT        NOT NULL COMMENT '关联 client_user_base.id',
    account_type        VARCHAR(32)   NOT NULL COMMENT '账号类型：USERNAME / PHONE / OAUTH',
    account_identifier  VARCHAR(128)  NOT NULL COMMENT '账号标识',
    oauth_provider      VARCHAR(64)            COMMENT 'OAuth 提供商标识，如 wechat-miniprogram',
    oauth_subject       VARCHAR(256)           COMMENT 'OAuth 用户唯一标识',
    credential_meta     JSON                   COMMENT '扩展凭证元数据 JSON',
    verified            SMALLINT      NOT NULL DEFAULT 0 COMMENT '是否已验证：0-未验证，1-已验证',
    status              SMALLINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    is_deleted          SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_user_account_login (account_type, account_identifier, oauth_provider),
    KEY idx_client_user_account_user_id (user_id, is_deleted),
    CONSTRAINT fk_client_user_account_user_id FOREIGN KEY (user_id) REFERENCES client_user_base (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户端登录账号';

-- 用户端密码（一用户一条）
CREATE TABLE IF NOT EXISTS client_user_password (
    id                BIGINT        NOT NULL COMMENT '密码记录主键',
    user_id           BIGINT        NOT NULL COMMENT '关联 client_user_base.id',
    password_hash     VARCHAR(512)  NOT NULL COMMENT '密码哈希',
    encoder_id        VARCHAR(32)   NOT NULL COMMENT '密码编码器标识',
    password_version  INT           NOT NULL DEFAULT 1 COMMENT '密码版本号',
    is_deleted        SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_user_password_user_id (user_id),
    CONSTRAINT fk_client_user_password_user_id FOREIGN KEY (user_id) REFERENCES client_user_base (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户端密码';

-- 用户端站内信主记录
CREATE TABLE IF NOT EXISTS client_notification (
    id                BIGINT        NOT NULL COMMENT '主键',
    title             VARCHAR(128)  NOT NULL COMMENT '标题',
    content           VARCHAR(512)  NOT NULL COMMENT '正文',
    biz_type          VARCHAR(64)   NOT NULL COMMENT '业务类型',
    biz_id            BIGINT        NULL COMMENT '业务主键',
    extra_json        JSON          NULL COMMENT '扩展载荷 JSON',
    recipient_count   INT           NOT NULL DEFAULT 0 COMMENT '实际投递用户数',
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_client_notification_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户端站内信';

-- 用户端站内信收件记录
CREATE TABLE IF NOT EXISTS client_notification_recipient (
    id                BIGINT        NOT NULL COMMENT '主键',
    notification_id   BIGINT        NOT NULL COMMENT '通知主键',
    user_id           BIGINT        NOT NULL COMMENT '收件人用户 ID',
    read_status       SMALLINT      NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    read_time         TIMESTAMP     NULL COMMENT '阅读时间',
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_client_notification_recipient_user (user_id, read_status),
    KEY idx_client_notification_recipient_notice (notification_id),
    CONSTRAINT fk_client_notification_recipient_notice FOREIGN KEY (notification_id) REFERENCES client_notification (id),
    CONSTRAINT fk_client_notification_recipient_user FOREIGN KEY (user_id) REFERENCES client_user_base (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户端站内信收件';
