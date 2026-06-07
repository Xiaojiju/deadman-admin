package com.mtfm.deadman.common.spi;

import java.util.List;

/**
 * 用户角色分配 SPI，由 security 模块实现，供 system 在用户管理流程中调用。
 */
public interface UserRoleAssignment {

    /**
     * 为新用户绑定默认 USER 角色（若尚未绑定）。
     *
     * @param userId 用户 ID
     */
    void assignDefaultUserRole(Long userId);

    /**
     * 为用户分配角色（覆盖式）。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     */
    void assignUserRoles(Long userId, List<Long> roleIds);
}
