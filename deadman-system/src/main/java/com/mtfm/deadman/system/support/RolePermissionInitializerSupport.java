package com.mtfm.deadman.system.support;

import com.mtfm.deadman.system.entity.SysRolePermission;
import com.mtfm.deadman.system.mapper.SysRolePermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 启动时角色权限增量绑定工具，避免逐条 count 查库。
 */
@Slf4j
@Component
public class RolePermissionInitializerSupport {

    /**
     * 增量绑定角色权限，已存在则跳过（避免覆盖其他模块为同一角色绑定的权限）。
     *
     * @param rolePermissionMapper 角色权限 Mapper
     * @param roleId               角色 ID
     * @param permissionCodes      权限码列表
     * @return 是否新增了绑定
     */
    public boolean ensurePermissions(
            SysRolePermissionMapper rolePermissionMapper, Long roleId, List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return false;
        }
        Set<String> existing = new HashSet<>(rolePermissionMapper.selectPermissionCodesByRoleId(roleId));
        List<String> missing = permissionCodes.stream()
                .filter(code -> !existing.contains(code))
                .distinct()
                .toList();
        if (missing.isEmpty()) {
            return false;
        }
        for (String code : missing) {
            rolePermissionMapper.insert(
                    SysRolePermission.builder().roleId(roleId).permissionCode(code).build());
            log.info("已为角色 {} 绑定权限 {}", roleId, code);
        }
        return true;
    }
}
