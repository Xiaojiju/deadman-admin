package com.mtfm.deadman.system.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.system.permission.SystemPermissions;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.system.entity.SysRole;
import com.mtfm.deadman.system.entity.SysRolePermission;
import com.mtfm.deadman.system.mapper.SysRoleMapper;
import com.mtfm.deadman.system.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 启动时初始化系统内置角色与权限绑定。
 * <p>
 * SUPER_ADMIN 不在库中绑定权限码，鉴权由 {@code SUPER_ADMIN} 角色判定（见 security 模块方法安全表达式）。
 */
@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class RbacDataInitializer implements ApplicationRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final UserAuthorityCache userAuthorityCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        initSuperAdminRole();
        initDefaultUserRole();
        userAuthorityCache.evictAllUserAuthorities();
    }

    private void initSuperAdminRole() {
        SysRole role = ensureRole(
                SysRoleCodes.SUPER_ADMIN,
                "超级管理员",
                "拥有全部权限（按角色判定，不绑定权限码表），系统内置不可删除",
                1);
        clearRolePermissions(role.getId());
    }

    private void clearRolePermissions(Long roleId) {
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
    }

    private void initDefaultUserRole() {
        SysRole role = ensureRole(SysRoleCodes.USER, "普通用户", "注册后默认角色，系统内置不可删除", 1);
        List<String> codes = List.of(
                SystemPermissions.Auth.PASSWORD_CHANGE,
                SystemPermissions.Auth.PERMISSIONS_READ,
                SystemPermissions.User.PROFILE_READ,
                SystemPermissions.User.PROFILE_UPDATE);
        replaceAllPermissions(role.getId(), codes);
    }

    private SysRole ensureRole(String roleCode, String roleName, String description, int systemBuiltin) {
        SysRole existing = sysRoleMapper
                .selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (existing != null) {
            return existing;
        }
        SysRole role = SysRole.builder()
                .roleCode(roleCode)
                .roleName(roleName)
                .description(description)
                .status(UserStatus.ACTIVE.getValue())
                .systemBuiltin(systemBuiltin)
                .build();
        sysRoleMapper.insert(role);
        log.info("已初始化系统角色: {}", roleCode);
        return role;
    }

    private void replaceAllPermissions(Long roleId, List<String> permissionCodes) {
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        for (String code : permissionCodes) {
            sysRolePermissionMapper.insert(
                    SysRolePermission.builder().roleId(roleId).permissionCode(code).build());
        }
    }
}
