-- Deadman Admin 数据库初始化脚本（MySQL 8+）
-- 字段注释使用 COMMENT 语法，可在 information_schema.COLUMNS 中查看

-- 用户基础信息（用户 ID 以此表为准）
CREATE TABLE IF NOT EXISTS user_base (
    id              BIGINT       NOT NULL COMMENT '用户主键，雪花算法生成',
    user_code       VARCHAR(32)  NOT NULL COMMENT '对外用户编码，非主键，用于对外身份标识',
    nickname        VARCHAR(64)           COMMENT '用户昵称',
    avatar          VARCHAR(512)          COMMENT '头像 URL',
    department_id   BIGINT                COMMENT '所属部门 ID，关联 sys_department.id',
    status          SMALLINT     NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    is_deleted      SMALLINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_base_user_code (user_code),
    KEY idx_user_base_status (status, is_deleted),
    KEY idx_user_base_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息，一用户一条';

-- 用户账号（支持多种登录方式，一用户可多条）
CREATE TABLE IF NOT EXISTS user_account (
    id                  BIGINT        NOT NULL COMMENT '账号记录主键',
    user_id             BIGINT        NOT NULL COMMENT '关联 user_base.id',
    account_type        VARCHAR(32)   NOT NULL COMMENT '账号类型：USERNAME / PHONE / OAUTH 等',
    account_identifier  VARCHAR(128)  NOT NULL COMMENT '账号标识：用户名、手机号、OAuth subject 等',
    oauth_provider      VARCHAR(64)            COMMENT 'OAuth 提供商标识，如 wechat、github；非 OAuth 时为 NULL',
    oauth_subject       VARCHAR(256)           COMMENT 'OAuth 用户唯一标识（subject / openId）',
    credential_meta     JSON                   COMMENT '扩展凭证元数据 JSON，如 token 摘要、绑定时间等',
    verified            SMALLINT      NOT NULL DEFAULT 0 COMMENT '是否已验证：0-未验证，1-已验证',
    status              SMALLINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    is_deleted          SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_login (account_type, account_identifier, oauth_provider),
    KEY idx_user_account_user_id (user_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录账号，支持用户名/手机/OAuth 等';

-- 用户密码（一用户仅一条）
CREATE TABLE IF NOT EXISTS user_password (
    id                BIGINT        NOT NULL COMMENT '密码记录主键',
    user_id           BIGINT        NOT NULL COMMENT '关联 user_base.id，表级唯一',
    password_hash     VARCHAR(512)  NOT NULL COMMENT '密码哈希值，由对应 encoder 生成',
    encoder_id        VARCHAR(32)   NOT NULL COMMENT '密码编码器标识，对应 PasswordEncoderIds 常量',
    password_version  INT           NOT NULL DEFAULT 1 COMMENT '密码版本号，每次改密递增',
    is_deleted        SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_password_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户密码，一用户一条；encoder_id 记录所用 PasswordEncoder';

-- 角色
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT        NOT NULL COMMENT '角色主键',
    role_code       VARCHAR(64)   NOT NULL COMMENT '角色编码，如 SUPER_ADMIN、USER',
    role_name       VARCHAR(64)   NOT NULL COMMENT '角色名称',
    description     VARCHAR(256)           COMMENT '角色描述',
    status          SMALLINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    system_builtin  SMALLINT      NOT NULL DEFAULT 0 COMMENT '是否系统内置：1-不可删除',
    is_deleted      SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

-- 用户角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT    NOT NULL COMMENT '关联主键',
    user_id     BIGINT    NOT NULL COMMENT '用户主键，关联 user_base.id',
    role_id     BIGINT    NOT NULL COMMENT '角色主键，关联 sys_role.id',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与角色多对多关联';

-- 用户数据权限（与角色独立）
CREATE TABLE IF NOT EXISTS user_data_scope (
    id          BIGINT       NOT NULL COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户 ID，关联 user_base.id',
    scope_type  VARCHAR(32)  NOT NULL DEFAULT 'DEPT' COMMENT '数据范围：ALL/DEPT/DEPT_AND_CHILD/SELF/CUSTOM',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_data_scope_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户数据权限配置，与角色独立';

-- 用户 CUSTOM 数据权限可见部门
CREATE TABLE IF NOT EXISTS user_data_scope_dept (
    id          BIGINT    NOT NULL COMMENT '主键',
    user_id     BIGINT    NOT NULL COMMENT '用户 ID，关联 user_base.id',
    dept_id     BIGINT    NOT NULL COMMENT '可见部门 ID，关联 sys_department.id',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_data_scope_dept (user_id, dept_id),
    KEY idx_user_data_scope_dept_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户 CUSTOM 数据权限可见部门';

-- 角色权限码关联（权限码来自枚举，不在此表维护定义）
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT        NOT NULL COMMENT '关联主键',
    role_id         BIGINT        NOT NULL COMMENT '角色主键，关联 sys_role.id',
    permission_code VARCHAR(128)  NOT NULL COMMENT '权限码，对应 PermissionCode 枚举',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色与权限码关联，权限定义由代码枚举维护';

-- 部门（树形组织结构）
CREATE TABLE IF NOT EXISTS sys_department (
    id              BIGINT        NOT NULL COMMENT '部门主键',
    parent_id       BIGINT                 COMMENT '上级部门 ID，NULL 表示根部门',
    dept_code       VARCHAR(64)   NOT NULL COMMENT '部门编码',
    dept_name       VARCHAR(128)  NOT NULL COMMENT '部门名称',
    sort_order      INT           NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    status          SMALLINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_deleted      SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_department_code (dept_code),
    KEY idx_sys_department_parent (parent_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织部门';

-- 职位（可归属部门）
CREATE TABLE IF NOT EXISTS sys_position (
    id              BIGINT        NOT NULL COMMENT '职位主键',
    department_id   BIGINT                 COMMENT '所属部门 ID，NULL 表示全局职位',
    position_code   VARCHAR(64)   NOT NULL COMMENT '职位编码',
    position_name   VARCHAR(128)  NOT NULL COMMENT '职位名称',
    sort_order      INT           NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    status          SMALLINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_deleted      SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_position_code (position_code),
    KEY idx_sys_position_department (department_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位';

-- 用户职位关联（一用户可绑定多个职位）
CREATE TABLE IF NOT EXISTS sys_user_position (
    id              BIGINT    NOT NULL COMMENT '关联主键',
    user_id         BIGINT    NOT NULL COMMENT '用户主键，关联 user_base.id',
    position_id     BIGINT    NOT NULL COMMENT '职位主键，关联 sys_position.id',
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_position (user_id, position_id),
    KEY idx_sys_user_position_user (user_id),
    KEY idx_sys_user_position_position (position_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与职位多对多关联';

-- 用户端组件表结构见 deadman-component-client：
-- components/deadman-component-client/src/main/resources/db/client/schema.sql

-- WebSocket 插件：消息持久化
CREATE TABLE IF NOT EXISTS plugin_ws_message (
    id              BIGINT        NOT NULL COMMENT '主键',
    message_id      VARCHAR(64)   NOT NULL COMMENT '业务消息 ID，全局唯一',
    channel_code    VARCHAR(32)   NOT NULL COMMENT '消息通道编码',
    message_type    VARCHAR(64)   NOT NULL COMMENT '消息类型',
    target_user_key VARCHAR(128)  NOT NULL COMMENT '目标用户在通道内的标识',
    payload_json    JSON                   COMMENT '扩展负载 JSON',
    status          SMALLINT      NOT NULL DEFAULT 0 COMMENT '0-待发送 1-已发送 2-失败 3-重试中',
    retry_count     INT           NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retry       INT           NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time TIMESTAMP              COMMENT '下次重试时间',
    error_message   VARCHAR(512)           COMMENT '最近失败原因',
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version         INT           NOT NULL DEFAULT 0 COMMENT '乐观锁',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_ws_message_id (message_id),
    KEY idx_plugin_ws_message_retry (status, next_retry_time),
    KEY idx_plugin_ws_message_target (channel_code, target_user_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WebSocket 消息投递记录';

-- 站内信通知
CREATE TABLE IF NOT EXISTS sys_notification (
    id                  BIGINT        NOT NULL COMMENT '主键',
    title               VARCHAR(200)  NOT NULL COMMENT '标题',
    content             TEXT          NOT NULL COMMENT '正文',
    target_type         SMALLINT      NOT NULL COMMENT '1-用户 2-部门 3-职位 4-全体',
    target_payload_json JSON                   COMMENT '目标参数 JSON',
    sender_user_id      BIGINT                 COMMENT '发送人用户 ID',
    recipient_count     INT           NOT NULL DEFAULT 0 COMMENT '投递用户数',
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_notification_sender (sender_user_id),
    KEY idx_sys_notification_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信通知';

CREATE TABLE IF NOT EXISTS sys_notification_recipient (
    id              BIGINT    NOT NULL COMMENT '主键',
    notification_id BIGINT    NOT NULL COMMENT '通知主键',
    user_id         BIGINT    NOT NULL COMMENT '收件人用户 ID',
    read_status     SMALLINT  NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
    read_time       TIMESTAMP          COMMENT '阅读时间',
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_notification_recipient (notification_id, user_id),
    KEY idx_sys_notification_recipient_user (user_id, read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信收件人';
