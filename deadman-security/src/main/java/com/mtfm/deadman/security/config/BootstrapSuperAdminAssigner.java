package com.mtfm.deadman.security.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.security.service.AuthCredentialsService;
import com.mtfm.deadman.security.service.AuthPermissionService;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.service.RoleAdminService;
import com.mtfm.deadman.system.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 启动时确保存在超级管理员：若系统中尚无 SUPER_ADMIN 用户，则按配置创建或绑定。
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class BootstrapSuperAdminAssigner implements ApplicationRunner {

    private final DeadmanProperties deadmanProperties;
    private final RoleAdminService roleAdminService;
    private final UserAccountService userAccountService;
    private final AuthCredentialsService authCredentialsService;
    private final AuthPermissionService authPermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        DeadmanProperties.Bootstrap bootstrap = deadmanProperties.getBootstrap();
        if (!bootstrap.isAdminEnabled()) {
            return;
        }
        if (roleAdminService.existsSuperAdminUser()) {
            log.debug("已存在 SUPER_ADMIN 用户，跳过引导");
            return;
        }

        String username = bootstrap.getSuperAdminUsername();
        if (!StringUtils.hasText(username)) {
            log.warn("系统中无超级管理员，且未配置 deadman.bootstrap.super-admin-username，跳过引导");
            return;
        }

        UserAccount account = userAccountService.getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getAccountType, AccountType.USERNAME.getCode())
                .eq(UserAccount::getAccountIdentifier, username));

        if (account != null) {
            roleAdminService.assignSuperAdminRole(account.getUserId());
            authPermissionService.evictUserAuthorities(account.getUserId());
            log.info("已为用户 {} 绑定超级管理员角色", username);
            return;
        }

        String password = bootstrap.getSuperAdminPassword();
        if (!StringUtils.hasText(password)) {
            log.warn(
                    "系统中无超级管理员，用户 {} 不存在，且未配置 deadman.bootstrap.super-admin-password，无法自动创建",
                    username);
            return;
        }

        String userCode = authCredentialsService.bootstrapSuperAdmin(
                username, password, bootstrap.getSuperAdminNickname());
        log.info("已自动创建超级管理员：username={}，userCode={}", username, userCode);
    }
}
