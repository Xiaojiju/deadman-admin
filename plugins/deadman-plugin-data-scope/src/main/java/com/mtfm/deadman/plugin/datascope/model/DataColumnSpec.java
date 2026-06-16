package com.mtfm.deadman.plugin.datascope.model;

/**
 * 单表数据权限列映射。
 *
 * @param tableName            物理表名（小写）
 * @param deptColumn           主表部门列名
 * @param userColumn           主表用户列名
 * @param deptJoinTable        部门关联表名
 * @param deptJoinUserColumn   关联表用户列名
 * @param deptJoinDeptColumn   关联表部门列名
 * @param deptJoinPrimaryOnly  是否仅匹配主部门
 */
public record DataColumnSpec(
        String tableName,
        String deptColumn,
        String userColumn,
        String deptJoinTable,
        String deptJoinUserColumn,
        String deptJoinDeptColumn,
        boolean deptJoinPrimaryOnly) {

    /**
     * 是否配置了任意过滤列或关联表。
     *
     * @return 有过滤配置时返回 true
     */
    public boolean hasAnyColumn() {
        return (deptColumn != null && !deptColumn.isBlank())
                || (userColumn != null && !userColumn.isBlank())
                || (deptJoinTable != null && !deptJoinTable.isBlank());
    }
}
