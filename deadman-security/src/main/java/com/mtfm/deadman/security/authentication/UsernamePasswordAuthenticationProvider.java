package com.mtfm.deadman.security.authentication;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.service.AuthPermissionService;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserAccountService;
import com.mtfm.deadman.system.service.UserPasswordService;
import com.mtfm.deadman.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证：按登录账号校验密码并构建 {@link LoginUser}。
 */
@Component
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserAccountService userAccountService;
    private final UserService userService;
    private final UserPasswordService userPasswordService;
    private final AuthPermissionService authPermissionService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginUsername = authentication.getName();
        String rawPassword = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();
        if (rawPassword == null) {
            throw new BadCredentialsException("密码不能为空");
        }

        UserAccount account = userAccountService.findByUsername(loginUsername);
        if (account == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        UserBase userBase = userService.getById(account.getUserId());
        if (userBase == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }
        try {
            if (!userPasswordService.matches(userBase.getId(), rawPassword)) {
                throw new BadCredentialsException("用户名或密码错误");
            }
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("用户名或密码错误", ex);
        }

        LoginUser loginUser = authPermissionService.buildLoginUser(userBase);
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
