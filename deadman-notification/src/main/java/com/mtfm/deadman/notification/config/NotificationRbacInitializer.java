package com.mtfm.deadman.notification.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.notification.permission.NotificationPermissions;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.system.entity.SysRole;
import com.mtfm.deadman.system.mapper.SysRoleMapper;
import com.mtfm.deadman.system.mapper.SysRolePermissionMapper;
import com.mtfm.deadman.system.support.RolePermissionInitializerSupport;
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
    private final RolePermissionInitializerSupport rolePermissionInitializerSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        SysRole userRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, SysRoleCodes.USER));
        if (userRole == null) {
            return;
        }
        boolean changed = rolePermissionInitializerSupport.ensurePermissions(
                sysRolePermissionMapper,
                userRole.getId(),
                List.of(NotificationPermissions.INBOX_READ, NotificationPermissions.INBOX_UPDATE));
        if (changed) {
            userAuthorityCache.evictAllUserAuthorities();
        }
    }
}
