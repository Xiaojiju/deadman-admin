-- deadman-component-client 集成测试用 H2（MySQL 兼容模式），结构与 schema.sql 一致

CREATE TABLE IF NOT EXISTS client_user_base (
    id              BIGINT       NOT NULL,
    user_code       VARCHAR(32)  NOT NULL,
    nickname        VARCHAR(64),
    avatar          VARCHAR(512),
    status          SMALLINT     NOT NULL DEFAULT 1,
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_user_base_user_code UNIQUE (user_code)
);

CREATE TABLE IF NOT EXISTS client_user_account (
    id                  BIGINT        NOT NULL,
    user_id             BIGINT        NOT NULL,
    account_type        VARCHAR(32)   NOT NULL,
    account_identifier  VARCHAR(128)  NOT NULL,
    oauth_provider      VARCHAR(64),
    oauth_subject       VARCHAR(256),
    credential_meta     JSON,
    verified            SMALLINT      NOT NULL DEFAULT 0,
    status              SMALLINT      NOT NULL DEFAULT 1,
    is_deleted          SMALLINT      NOT NULL DEFAULT 0,
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_user_account_login UNIQUE (account_type, account_identifier, oauth_provider)
);

CREATE TABLE IF NOT EXISTS client_user_password (
    id                BIGINT        NOT NULL,
    user_id           BIGINT        NOT NULL,
    password_hash     VARCHAR(512)  NOT NULL,
    encoder_id        VARCHAR(32)   NOT NULL,
    password_version  INT           NOT NULL DEFAULT 1,
    is_deleted        SMALLINT      NOT NULL DEFAULT 0,
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_user_password_user_id UNIQUE (user_id)
);
