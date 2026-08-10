-- =============================================================================
-- 组织模型升级：user_base.department_id → sys_user_department；sys_user_position 增加 department_id
-- 适用：升级前已部署旧版 schema 的 MySQL 8+ 环境（全新安装请直接执行 schema.sql，无需本脚本）
-- 可重复执行：已完成的步骤会自动跳过
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 确保 sys_user_department 表存在
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_department (
    id              BIGINT    NOT NULL COMMENT '关联主键',
    user_id         BIGINT    NOT NULL COMMENT '用户主键，关联 user_base.id',
    dept_id         BIGINT    NOT NULL COMMENT '部门主键，关联 sys_department.id',
    is_primary      SMALLINT  NOT NULL DEFAULT 0 COMMENT '是否主部门：0-否，1-是',
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_department (user_id, dept_id),
    KEY idx_sys_user_department_user (user_id),
    KEY idx_sys_user_department_dept (dept_id),
    KEY idx_sys_user_department_primary (user_id, is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与部门多对多关联';

-- -----------------------------------------------------------------------------
-- 2. user_base.department_id → sys_user_department（列存在时执行）
-- -----------------------------------------------------------------------------
SET @has_user_base_dept_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_base'
      AND COLUMN_NAME = 'department_id'
);

SET @migrate_user_dept_sql := IF(
    @has_user_base_dept_col > 0,
    'INSERT INTO sys_user_department (id, user_id, dept_id, is_primary, create_time)
     SELECT base.max_id + ROW_NUMBER() OVER (ORDER BY ub.id),
            ub.id,
            ub.department_id,
            1,
            CURRENT_TIMESTAMP
     FROM user_base ub
     CROSS JOIN (SELECT IFNULL(MAX(id), 0) AS max_id FROM sys_user_department) base
     WHERE ub.department_id IS NOT NULL
       AND ub.is_deleted = 0
       AND NOT EXISTS (
           SELECT 1 FROM sys_user_department ud
           WHERE ud.user_id = ub.id AND ud.dept_id = ub.department_id
       )',
    'SELECT 1'
);
PREPARE stmt FROM @migrate_user_dept_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 3. sys_user_position 增加 department_id 并回填（列不存在时执行）
-- -----------------------------------------------------------------------------
SET @has_user_position_dept_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_position'
      AND COLUMN_NAME = 'department_id'
);

SET @add_user_position_dept_col_sql := IF(
    @has_user_position_dept_col = 0,
    'ALTER TABLE sys_user_position ADD COLUMN department_id BIGINT NULL COMMENT ''部门主键，关联 sys_department.id'' AFTER user_id',
    'SELECT 1'
);
PREPARE stmt FROM @add_user_position_dept_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 回填 department_id：优先主部门 → user_base.department_id → 职位所属部门
SET @backfill_user_position_dept_sql := IF(
    @has_user_position_dept_col = 0,
    'UPDATE sys_user_position up
     LEFT JOIN user_base ub ON ub.id = up.user_id
     LEFT JOIN sys_user_department ud ON ud.user_id = up.user_id AND ud.is_primary = 1
     LEFT JOIN sys_position p ON p.id = up.position_id
     SET up.department_id = COALESCE(ud.dept_id, ub.department_id, p.department_id)
     WHERE up.department_id IS NULL',
    'SELECT 1'
);
PREPARE stmt FROM @backfill_user_position_dept_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 无法推断部门的职位绑定将被删除（需人工核对后重新绑定）
DELETE FROM sys_user_position WHERE department_id IS NULL;

SET @enforce_user_position_dept_not_null_sql := IF(
    @has_user_position_dept_col = 0,
    'ALTER TABLE sys_user_position MODIFY COLUMN department_id BIGINT NOT NULL COMMENT ''部门主键，关联 sys_department.id''',
    'SELECT 1'
);
PREPARE stmt FROM @enforce_user_position_dept_not_null_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 旧唯一键 (user_id, position_id) → (user_id, department_id, position_id)
SET @has_old_user_position_uk := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_position'
      AND INDEX_NAME = 'uk_sys_user_position'
      AND SEQ_IN_INDEX = 2
      AND COLUMN_NAME = 'position_id'
      AND NOT EXISTS (
          SELECT 1
          FROM information_schema.STATISTICS s2
          WHERE s2.TABLE_SCHEMA = DATABASE()
            AND s2.TABLE_NAME = 'sys_user_position'
            AND s2.INDEX_NAME = 'uk_sys_user_position'
            AND s2.SEQ_IN_INDEX = 2
            AND s2.COLUMN_NAME = 'department_id'
      )
);

SET @drop_old_user_position_uk_sql := IF(
    @has_old_user_position_uk > 0,
    'ALTER TABLE sys_user_position DROP INDEX uk_sys_user_position',
    'SELECT 1'
);
PREPARE stmt FROM @drop_old_user_position_uk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_new_user_position_uk := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_position'
      AND INDEX_NAME = 'uk_sys_user_position'
);

SET @add_new_user_position_uk_sql := IF(
    @has_new_user_position_uk = 0,
    'ALTER TABLE sys_user_position ADD UNIQUE KEY uk_sys_user_position (user_id, department_id, position_id)',
    'SELECT 1'
);
PREPARE stmt FROM @add_new_user_position_uk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_user_position_dept_idx := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_position'
      AND INDEX_NAME = 'idx_sys_user_position_dept'
);

SET @add_user_position_dept_idx_sql := IF(
    @has_user_position_dept_idx = 0,
    'ALTER TABLE sys_user_position ADD KEY idx_sys_user_position_dept (department_id)',
    'SELECT 1'
);
PREPARE stmt FROM @add_user_position_dept_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 4. 删除 user_base.department_id
-- -----------------------------------------------------------------------------
SET @drop_user_base_dept_col_sql := IF(
    @has_user_base_dept_col > 0,
    'ALTER TABLE user_base DROP COLUMN department_id',
    'SELECT 1'
);
PREPARE stmt FROM @drop_user_base_dept_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
