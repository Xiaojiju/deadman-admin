-- 平台股东档案、股东员工关系，以及推广绑定股东快照
CREATE TABLE IF NOT EXISTS platform_shareholder (
    id                              BIGINT         NOT NULL COMMENT '主键',
    client_user_id                  BIGINT         NOT NULL COMMENT '股东用户 ID（client 用户）',
    invite_code                     VARCHAR(16)    NOT NULL COMMENT '入职码（小程序 scene 载荷）',
    status                          VARCHAR(32)    NOT NULL COMMENT '状态：active/revoked',
    participate_register_reward     TINYINT        NOT NULL DEFAULT 1 COMMENT '是否参与普通用户拉新奖励：0-否 1-是',
    participate_membership_first    TINYINT        NOT NULL DEFAULT 1 COMMENT '是否参与普通用户会员首购抽成：0-否 1-是',
    appointed_by_admin_id           BIGINT         NULL COMMENT '指定人管理端用户 ID',
    appointed_at                    DATETIME       NULL COMMENT '指定时间',
    revoked_by_admin_id             BIGINT         NULL COMMENT '撤销人管理端用户 ID',
    revoked_at                      DATETIME       NULL COMMENT '撤销时间',
    remark                          VARCHAR(512)   NULL COMMENT '备注',
    create_time                     DATETIME       NOT NULL COMMENT '创建时间',
    update_time                     DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_shareholder_user (client_user_id),
    UNIQUE KEY uk_platform_shareholder_invite (invite_code),
    KEY idx_platform_shareholder_status (status, appointed_at)
) COMMENT '平台股东档案（一个 C 端用户一份）';

CREATE TABLE IF NOT EXISTS platform_shareholder_employee (
    id                              BIGINT         NOT NULL COMMENT '主键',
    shareholder_id                  BIGINT         NOT NULL COMMENT '股东档案 ID',
    client_user_id                  BIGINT         NOT NULL COMMENT '员工用户 ID（client 用户）',
    employee_name                   VARCHAR(64)    NOT NULL COMMENT '员工姓名',
    phone                           VARCHAR(32)    NOT NULL COMMENT '员工电话',
    avatar_file_id                  BIGINT         NULL COMMENT '员工头像文件 ID',
    status                          VARCHAR(32)    NOT NULL COMMENT '状态：pending/active/rejected/left',
    participate_register_reward     TINYINT        NOT NULL DEFAULT 1 COMMENT '是否参与普通用户拉新奖励：0-否 1-是',
    participate_membership_first    TINYINT        NOT NULL DEFAULT 1 COMMENT '是否参与普通用户会员首购抽成：0-否 1-是',
    audit_remark                    VARCHAR(512)   NULL COMMENT '审核备注（驳回时）',
    audit_time                      DATETIME       NULL COMMENT '审核时间',
    leave_time                      DATETIME       NULL COMMENT '离职时间',
    create_time                     DATETIME       NOT NULL COMMENT '创建时间',
    update_time                     DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_shareholder_employee (shareholder_id, client_user_id),
    KEY idx_pse_user_status (client_user_id, status),
    KEY idx_pse_shareholder_status (shareholder_id, status, create_time)
) COMMENT '平台股东员工关系';

ALTER TABLE user_promo_bind
    ADD COLUMN shareholder_id BIGINT NULL COMMENT '绑定时平台股东档案 ID 快照' AFTER merchant_profile_id,
    ADD COLUMN shareholder_employee_id BIGINT NULL COMMENT '绑定时股东员工关系 ID 快照' AFTER shareholder_id,
    ADD KEY idx_user_promo_bind_shareholder (shareholder_id, create_time);
