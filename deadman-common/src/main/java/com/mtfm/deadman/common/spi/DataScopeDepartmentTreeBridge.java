package com.mtfm.deadman.common.spi;

import java.util.Set;

/**
 * 数据权限插件访问部门树的 SPI，由 system 模块实现。
 */
public interface DataScopeDepartmentTreeBridge {

    /**
     * 解析本部门及所有下级部门 ID。
     *
     * @param departmentId 根部门 ID
     * @return 部门 ID 集合
     */
    Set<Long> resolveSelfAndDescendantIds(Long departmentId);
}
