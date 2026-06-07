package com.mtfm.deadman.notification.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.notification.permission.NotificationPermissions;
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
 * 初始化站内信相关权限绑定。
 */
@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class NotificationRbacInitializer implements ApplicationRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final UserAuthorityCache userAuthorityCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        SysRole userRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, SysRoleCodes.USER));
        if (userRole != null) {
            ensurePermissions(userRole.getId(), List.of(
                    NotificationPermissions.INBOX_READ,
                    NotificationPermissions.INBOX_UPDATE));
        }
        userAuthorityCache.evictAllUserAuthorities();
    }

    private void ensurePermissions(Long roleId, List<String> permissionCodes) {
        for (String code : permissionCodes) {
            long count = sysRolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, roleId)
                    .eq(SysRolePermission::getPermissionCode, code));
            if (count == 0) {
                sysRolePermissionMapper.insert(
                        SysRolePermission.builder().roleId(roleId).permissionCode(code).build());
                log.info("已为角色 {} 绑定权限 {}", roleId, code);
            }
        }
    }
}
