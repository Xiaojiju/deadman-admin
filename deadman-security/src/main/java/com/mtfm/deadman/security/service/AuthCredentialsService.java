package com.mtfm.deadman.security.service;

import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.security.dto.auth.ChangePasswordRequest;
import com.mtfm.deadman.security.dto.auth.RegisterRequest;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserAccountService;
import com.mtfm.deadman.system.service.UserPasswordService;
import com.mtfm.deadman.system.service.UserService;
import com.mtfm.deadman.system.util.UserCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证凭证：注册、登录、改密（签发 JWT）。
 */
@Service
@RequiredArgsConstructor
public class AuthCredentialsService {

    private final UserService userService;
    private final UserAccountService userAccountService;
    private final UserPasswordService userPasswordService;
    private final RoleAdminService roleAdminService;
    private final AuthTokenService authTokenService;
    private final DeadmanProperties deadmanProperties;

    /**
     * 用户注册并签发 JWT。
     *
     * @param request 注册请求
     * @return 访问令牌及用户摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthTokenVO register(RegisterRequest request) {
        if (userAccountService.existsUsername(request.username())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        String userCode = UserCodeGenerator.generate(deadmanProperties.getUser().getUserCodePrefix());

        UserBase userBase = UserBase.builder()
                .userCode(userCode)
                .nickname(request.nickname() != null ? request.nickname() : request.username())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userService.save(userBase);

        UserAccount account = UserAccount.builder()
                .userId(userBase.getId())
                .accountType(AccountType.USERNAME.getCode())
                .accountIdentifier(request.username())
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userAccountService.save(account);

        userPasswordService.createPassword(userBase.getId(), request.password());
        roleAdminService.assignDefaultUserRole(userBase.getId());

        return authTokenService.issueToken(userBase);
    }

    /**
     * 修改当前用户密码。
     *
     * @param userId   用户 ID
     * @param userCode 用户编码（用于失效资料缓存）
     * @param request  原密码与新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String userCode, ChangePasswordRequest request) {
        userPasswordService.changePassword(userId, request.oldPassword(), request.newPassword());
        userService.evictProfileCache(userCode);
    }

    /**
     * 启动引导：创建超级管理员账号（仅 SUPER_ADMIN 角色，不含默认 USER 角色）。
     *
     * @param username 登录用户名
     * @param password 初始密码
     * @param nickname 昵称，可为空
     * @return 对外 userCode
     */
    @Transactional(rollbackFor = Exception.class)
    public String bootstrapSuperAdmin(String username, String password, String nickname) {
        if (userAccountService.existsUsername(username)) {
            throw new IllegalStateException("引导创建失败，用户名已存在: " + username);
        }

        String userCode = UserCodeGenerator.generate(deadmanProperties.getUser().getUserCodePrefix());
        String displayName = StringUtils.hasText(nickname) ? nickname : username;

        UserBase userBase = UserBase.builder()
                .userCode(userCode)
                .nickname(displayName)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userService.save(userBase);

        UserAccount account = UserAccount.builder()
                .userId(userBase.getId())
                .accountType(AccountType.USERNAME.getCode())
                .accountIdentifier(username)
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userAccountService.save(account);

        userPasswordService.createPassword(userBase.getId(), password);
        roleAdminService.assignSuperAdminRole(userBase.getId());

        return userCode;
    }

}
