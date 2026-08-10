-- 商户员工体系：店铺码 + 员工关系 + 运行时配置

ALTER TABLE merchant_profile
    ADD COLUMN shop_code VARCHAR(32) NULL COMMENT '店铺码（小程序码 scene / 员工入职识别）' AFTER status;

-- 历史店铺先用 ID 回填（雪花 ID 字符串长度在 32 以内），保证非空与唯一
UPDATE merchant_profile
SET shop_code = CAST(id AS CHAR)
WHERE shop_code IS NULL OR shop_code = '';

ALTER TABLE merchant_profile
    MODIFY COLUMN shop_code VARCHAR(32) NOT NULL COMMENT '店铺码（小程序码 scene / 员工入职识别）';

ALTER TABLE merchant_profile
    ADD UNIQUE KEY uk_merchant_profile_shop_code (shop_code);

CREATE TABLE IF NOT EXISTS merchant_employee (
    id                      BIGINT         NOT NULL COMMENT '主键',
    merchant_profile_id     BIGINT         NOT NULL COMMENT '店铺资料 ID',
    client_user_id          BIGINT         NOT NULL COMMENT '员工用户 ID（client 用户）',
    employee_name           VARCHAR(64)    NOT NULL COMMENT '员工姓名',
    phone                   VARCHAR(32)    NOT NULL COMMENT '员工电话',
    avatar_url              VARCHAR(512)   NULL COMMENT '员工头像 URL',
    status                  VARCHAR(32)    NOT NULL COMMENT '状态：pending/active/rejected/left',
    audit_remark            VARCHAR(512)   NULL COMMENT '审核备注（驳回时）',
    audit_time              DATETIME       NULL COMMENT '审核时间',
    leave_time              DATETIME       NULL COMMENT '离职时间',
    create_time             DATETIME       NOT NULL COMMENT '创建时间',
    update_time             DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_employee_profile_user (merchant_profile_id, client_user_id),
    KEY idx_merchant_employee_user_status (client_user_id, status),
    KEY idx_merchant_employee_profile_status (merchant_profile_id, status, create_time)
) COMMENT '商户员工关系（按店铺一份档案）';

CREATE TABLE IF NOT EXISTS merchant_employee_config (
    id                      BIGINT         NOT NULL COMMENT '主键，固定 1',
    max_merchant_joins      INT            NOT NULL DEFAULT 3 COMMENT '同一员工最多挂靠商户数（pending+active）',
    update_time             DATETIME       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) COMMENT '商户员工运行时配置（单行）';

INSERT INTO merchant_employee_config (id, max_merchant_joins, update_time)
VALUES (1, 3, NOW())
ON DUPLICATE KEY UPDATE id = id;
