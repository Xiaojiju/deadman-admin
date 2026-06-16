-- 集成测试用 H2（MySQL 兼容模式），结构与 schema.sql 一致
-- 用户端组件表见 classpath:db/client/schema-h2.sql（deadman-component-client 模块）

CREATE TABLE IF NOT EXISTS user_base (
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
    CONSTRAINT uk_user_base_user_code UNIQUE (user_code)
);

CREATE TABLE IF NOT EXISTS user_account (
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
    CONSTRAINT uk_user_account_login UNIQUE (account_type, account_identifier, oauth_provider)
);

CREATE TABLE IF NOT EXISTS user_password (
    id                BIGINT        NOT NULL,
    user_id           BIGINT        NOT NULL,
    password_hash     VARCHAR(512)  NOT NULL,
    encoder_id        VARCHAR(32)   NOT NULL,
    password_version  INT           NOT NULL DEFAULT 1,
    is_deleted        SMALLINT      NOT NULL DEFAULT 0,
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_password_user_id UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT        NOT NULL,
    role_code       VARCHAR(64)   NOT NULL,
    role_name       VARCHAR(64)   NOT NULL,
    description     VARCHAR(256),
    status          SMALLINT      NOT NULL DEFAULT 1,
    system_builtin  SMALLINT      NOT NULL DEFAULT 0,
    is_deleted      SMALLINT      NOT NULL DEFAULT 0,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS user_data_scope (
    id          BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    scope_type  VARCHAR(32)  NOT NULL DEFAULT 'DEPT',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_data_scope_user UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS user_data_scope_dept (
    id          BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    dept_id     BIGINT    NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_data_scope_dept UNIQUE (user_id, dept_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT        NOT NULL,
    role_id         BIGINT        NOT NULL,
    permission_code VARCHAR(128)  NOT NULL,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_code)
);

CREATE TABLE IF NOT EXISTS sys_department (
    id              BIGINT        NOT NULL,
    parent_id       BIGINT,
    dept_code       VARCHAR(64)   NOT NULL,
    dept_name       VARCHAR(128)  NOT NULL,
    sort_order      INT           NOT NULL DEFAULT 0,
    status          SMALLINT      NOT NULL DEFAULT 1,
    is_deleted      SMALLINT      NOT NULL DEFAULT 0,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_department_code UNIQUE (dept_code)
);

CREATE TABLE IF NOT EXISTS sys_position (
    id              BIGINT        NOT NULL,
    department_id   BIGINT,
    position_code   VARCHAR(64)   NOT NULL,
    position_name   VARCHAR(128)  NOT NULL,
    sort_order      INT           NOT NULL DEFAULT 0,
    status          SMALLINT      NOT NULL DEFAULT 1,
    is_deleted      SMALLINT      NOT NULL DEFAULT 0,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_position_code UNIQUE (position_code)
);

CREATE TABLE IF NOT EXISTS sys_user_department (
    id              BIGINT    NOT NULL,
    user_id         BIGINT    NOT NULL,
    dept_id         BIGINT    NOT NULL,
    is_primary      SMALLINT  NOT NULL DEFAULT 0,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_department UNIQUE (user_id, dept_id)
);

CREATE TABLE IF NOT EXISTS sys_user_position (
    id              BIGINT    NOT NULL,
    user_id         BIGINT    NOT NULL,
    department_id   BIGINT    NOT NULL,
    position_id     BIGINT    NOT NULL,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_position UNIQUE (user_id, department_id, position_id)
);

CREATE TABLE IF NOT EXISTS plugin_ws_message (
    id              BIGINT        NOT NULL,
    message_id      VARCHAR(64)   NOT NULL,
    channel_code    VARCHAR(32)   NOT NULL,
    message_type    VARCHAR(64)   NOT NULL,
    target_user_key VARCHAR(128)  NOT NULL,
    payload_json    JSON,
    status          SMALLINT      NOT NULL DEFAULT 0,
    retry_count     INT           NOT NULL DEFAULT 0,
    max_retry       INT           NOT NULL DEFAULT 3,
    next_retry_time TIMESTAMP,
    error_message   VARCHAR(512),
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_ws_message_id UNIQUE (message_id)
);

CREATE TABLE IF NOT EXISTS sys_notification (
    id                  BIGINT        NOT NULL,
    title               VARCHAR(200)  NOT NULL,
    content             CLOB          NOT NULL,
    target_type         SMALLINT      NOT NULL,
    target_payload_json JSON,
    sender_user_id      BIGINT,
    recipient_count     INT           NOT NULL DEFAULT 0,
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS sys_notification_recipient (
    id              BIGINT    NOT NULL,
    notification_id BIGINT    NOT NULL,
    user_id         BIGINT    NOT NULL,
    read_status     SMALLINT  NOT NULL DEFAULT 0,
    read_time       TIMESTAMP,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_notification_recipient UNIQUE (notification_id, user_id)
);
