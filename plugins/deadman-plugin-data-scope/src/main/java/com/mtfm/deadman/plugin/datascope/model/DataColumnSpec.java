package com.mtfm.deadman.plugin.datascope.model;

/**
 * 单表数据权限列映射。
 *
 * @param tableName  物理表名（小写）
 * @param deptColumn 部门列名
 * @param userColumn 用户列名
 */
public record DataColumnSpec(String tableName, String deptColumn, String userColumn) {

    /**
     * 是否配置了任意过滤列。
     *
     * @return 有部门列或用户列时返回 true
     */
    public boolean hasAnyColumn() {
        return (deptColumn != null && !deptColumn.isBlank()) || (userColumn != null && !userColumn.isBlank());
    }
}
