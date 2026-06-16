package com.mtfm.deadman.system.aspect;

import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.system.entity.SysRole;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色分配保护辅助：供 {@link ProtectSuperAdminUser} SpEL 条件使用。
 */
@Component("roleAssignmentGuard")
@RequiredArgsConstructor
public class RoleAssignmentGuard {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleService sysRoleService;

    /**
     * 判断本次分配是否会移除用户的 SUPER_ADMIN 角色。
     *
     * @param userId  用户 ID
     * @param roleIds 新角色 ID 列表
     * @return 会移除 SUPER_ADMIN 时返回 true
     */
    public boolean wouldRemoveSuperAdmin(Long userId, List<Long> roleIds) {
        if (!sysUserRoleMapper.selectRoleCodesByUserId(userId).contains(SysRoleCodes.SUPER_ADMIN)) {
            return false;
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        SysRole superAdmin = sysRoleService.findByCode(SysRoleCodes.SUPER_ADMIN);
        return superAdmin == null || roleIds == null || !roleIds.contains(superAdmin.getId());
    }
}
