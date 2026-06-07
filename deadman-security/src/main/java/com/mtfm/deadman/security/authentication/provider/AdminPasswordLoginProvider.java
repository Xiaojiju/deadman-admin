package com.mtfm.deadman.security.authentication.provider;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.dto.auth.LoginRequest;
import com.mtfm.deadman.security.service.AuthPermissionService;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserAccountService;
import com.mtfm.deadman.system.service.UserPasswordService;
import com.mtfm.deadman.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 管理端用户名密码登录 Provider。
 */
@Component
@RequiredArgsConstructor
public class AdminPasswordLoginProvider implements LoginProvider {

    private final UserAccountService userAccountService;
    private final UserService userService;
    private final UserPasswordService userPasswordService;
    private final AuthPermissionService authPermissionService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return AdminAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return "password";
    }

    @Override
    public String customLoginEndpoint() {
        return "/api/auth/login";
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        LoginRequest loginRequest = parseLoginRequest(request);
        String username = loginRequest.username();
        String password = loginRequest.password();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new AuthenticationServiceException("用户名或密码不能为空");
        }
        return UsernamePasswordAuthenticationToken.unauthenticated(username.trim(), password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginUsername = authentication.getName();
        String rawPassword = authentication.getCredentials() == null
                ? null
                : authentication.getCredentials().toString();
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

    private LoginRequest parseLoginRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("登录请求解析失败", ex);
        }
    }
}
