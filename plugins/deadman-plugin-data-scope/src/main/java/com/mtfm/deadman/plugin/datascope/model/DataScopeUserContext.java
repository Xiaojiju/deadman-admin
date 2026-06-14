package com.mtfm.deadman.plugin.datascope.model;

import java.util.Collections;
import java.util.Set;

/**
 * 当前请求的数据权限用户上下文（登录后由插件预热缓存，Filter 注入 ThreadLocal）。
 *
 * @param userId         当前用户 ID
 * @param departmentId   当前用户所属部门 ID，可能为 null
 * @param scopeType      数据范围类型
 * @param visibleDeptIds 可见部门 ID 集合（DEPT / DEPT_AND_CHILD / CUSTOM 时使用）
 * @param bypass         是否跳过过滤（超管等）
 */
public record DataScopeUserContext(
        Long userId,
        Long departmentId,
        DataScopeType scopeType,
        Set<Long> visibleDeptIds,
        boolean bypass) {

    /**
     * 构造跳过过滤的上下文。
     *
     * @param userId 用户 ID
     * @return 跳过过滤上下文
     */
    public static DataScopeUserContext bypass(Long userId) {
        return new DataScopeUserContext(userId, null, DataScopeType.ALL, Collections.emptySet(), true);
    }

    /**
     * 可见部门 ID 集合（不可变）。
     *
     * @return 部门 ID 集合
     */
    @Override
    public Set<Long> visibleDeptIds() {
        return visibleDeptIds == null ? Set.of() : Set.copyOf(visibleDeptIds);
    }
}
