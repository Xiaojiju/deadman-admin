-- deadman-plugin-im-tencent IM 用户映射表（MySQL 8+）

CREATE TABLE IF NOT EXISTS plugin_im_user_account (
    id              BIGINT        NOT NULL COMMENT '主键',
    realm_id        VARCHAR(32)   NOT NULL COMMENT '用户域标识，如 client、admin',
    subject_id      VARCHAR(64)   NOT NULL COMMENT '域内稳定主键，如 userCode',
    im_user_id      VARCHAR(32)   NOT NULL COMMENT '腾讯云 IM UserID',
    nickname        VARCHAR(64)            COMMENT '昵称快照',
    avatar_url      VARCHAR(512)           COMMENT '头像 URL 快照',
    status          SMALLINT      NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    last_sync_time  TIMESTAMP              COMMENT '最近一次同步到腾讯云的时间',
    is_deleted      SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version         INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_im_realm_subject (realm_id, subject_id, is_deleted),
    UNIQUE KEY uk_plugin_im_user_id (im_user_id, is_deleted),
    KEY idx_plugin_im_realm (realm_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IM 用户映射';
