package com.mtfm.deadman.common.spi;

import java.util.List;

/**
 * 用户角色分配 SPI，由 system 模块的 {@code RoleAdminService} 实现，供用户管理流程调用。
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

    /**
     * 移除用户全部角色绑定（删除用户时使用）。
     *
     * @param userId 用户 ID
     */
    void removeAllUserRoles(Long userId);
}
