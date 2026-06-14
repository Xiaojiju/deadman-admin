package com.mtfm.deadman.plugin.datascope.model;

import java.util.Collections;
import java.util.Set;

/**
 * 用户数据权限配置快照（持久化层读模型）。
 *
 * @param scopeType     数据范围类型
 * @param customDeptIds CUSTOM 可见部门 ID 集合
 */
public record DataScopeProfile(DataScopeType scopeType, Set<Long> customDeptIds) {

    /** 未配置用户时的默认范围：本部门 */
    public static final DataScopeType DEFAULT_SCOPE_TYPE = DataScopeType.DEPT;

    /**
     * 默认配置（本部门、无自定义部门）。
     *
     * @return 默认配置
     */
    public static DataScopeProfile defaultProfile() {
        return new DataScopeProfile(DEFAULT_SCOPE_TYPE, Collections.emptySet());
    }
}
