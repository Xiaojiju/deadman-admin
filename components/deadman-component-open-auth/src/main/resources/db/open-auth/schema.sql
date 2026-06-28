-- 开放授权组件 DDL（MySQL 生产环境单独执行）

CREATE TABLE IF NOT EXISTS open_app (
    id                  BIGINT        NOT NULL COMMENT '主键',
    app_id              VARCHAR(32)   NOT NULL COMMENT '对外 AppId',
    app_name            VARCHAR(64)   NOT NULL COMMENT '应用名称',
    app_secret_hash     VARCHAR(128)  NOT NULL COMMENT 'client_secret 哈希',
    secret_encoder_id   VARCHAR(32)   NOT NULL COMMENT '密钥哈希编码器 ID',
    status              TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description         VARCHAR(256)  NULL COMMENT '应用说明',
    allowed_realms      VARCHAR(128)  NOT NULL COMMENT '允许的用户域，逗号分隔',
    default_scopes      VARCHAR(512)  NULL COMMENT '默认 scope 列表，逗号分隔',
    code_ttl_sec        INT           NOT NULL DEFAULT 300 COMMENT 'auth_code 有效期（秒）',
    token_ttl_sec       INT           NOT NULL DEFAULT 3600 COMMENT 'open_access_token 有效期（秒）',
    create_time         DATETIME      NOT NULL COMMENT '创建时间',
    update_time         DATETIME      NOT NULL COMMENT '更新时间',
    is_deleted          TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    version             INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_open_app_app_id (app_id),
    KEY idx_open_app_status (status, is_deleted)
) COMMENT '开放授权应用';
