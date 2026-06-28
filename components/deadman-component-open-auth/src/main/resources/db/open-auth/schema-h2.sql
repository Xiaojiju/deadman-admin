-- 开放授权组件 DDL（H2 集成测试）

CREATE TABLE IF NOT EXISTS open_app (
    id                  BIGINT        NOT NULL,
    app_id              VARCHAR(32)   NOT NULL,
    app_name            VARCHAR(64)   NOT NULL,
    app_secret_hash     VARCHAR(128)  NOT NULL,
    secret_encoder_id   VARCHAR(32)   NOT NULL,
    status              TINYINT       NOT NULL DEFAULT 1,
    description         VARCHAR(256)  NULL,
    allowed_realms      VARCHAR(128)  NOT NULL,
    default_scopes      VARCHAR(512)  NULL,
    code_ttl_sec        INT           NOT NULL DEFAULT 300,
    token_ttl_sec       INT           NOT NULL DEFAULT 3600,
    create_time         TIMESTAMP     NOT NULL,
    update_time         TIMESTAMP     NOT NULL,
    is_deleted          TINYINT       NOT NULL DEFAULT 0,
    version             INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_open_app_app_id (app_id)
);
