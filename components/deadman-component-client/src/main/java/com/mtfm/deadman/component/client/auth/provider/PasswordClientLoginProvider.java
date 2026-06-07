package com.mtfm.deadman.component.client.auth.provider;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.auth.token.ClientUsernamePasswordAuthenticationToken;
import com.mtfm.deadman.component.client.dto.ClientLoginPasswordRequest;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.component.client.service.ClientUserPasswordService;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.spi.ClientLoginProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 用户端用户名密码登录 Provider。
 */
@Component
@RequiredArgsConstructor
public class PasswordClientLoginProvider implements ClientLoginProvider {

    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserService clientUserService;
    private final ClientUserPasswordService clientUserPasswordService;
    private final JsonMapper jsonMapper;

    @Override
    public String providerId() {
        return "password";
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        ClientLoginPasswordRequest loginRequest = parseRequest(request);
        if (!StringUtils.hasText(loginRequest.username()) || !StringUtils.hasText(loginRequest.password())) {
            throw new AuthenticationServiceException("用户名或密码不能为空");
        }
        return new ClientUsernamePasswordAuthenticationToken(
                loginRequest.username().trim(), loginRequest.password());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
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

        ClientUserAccount account = clientUserAccountService.findByUsername(loginUsername);
        if (account == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        ClientUserBase userBase = clientUserService.getById(account.getUserId());
        if (userBase == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }
        try {
            if (!clientUserPasswordService.matches(userBase.getId(), rawPassword)) {
                throw new BadCredentialsException("用户名或密码错误");
            }
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("用户名或密码错误", ex);
        }

        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, loginUsername);
        return new ClientUsernamePasswordAuthenticationToken(loginUser, loginUser.getAuthorities());
    }

    private ClientLoginPasswordRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), ClientLoginPasswordRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("登录请求解析失败", ex);
        }
    }
}
