package com.mtfm.deadman.support.wechat.auth;

import com.mtfm.deadman.plugin.wechat.web.WechatWebConstants;
import com.mtfm.deadman.plugin.wechat.web.auth.WechatWebAuthenticationToken;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebLoginBinding;
import com.mtfm.deadman.plugin.wechat.web.dto.WechatWebLoginRequest;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.support.wechat.service.AdminWechatWebAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 管理端微信网页扫码登录 Provider，覆盖插件默认实现：未绑定时返回待绑定临时令牌而非自动开通用户。
 */
@RequiredArgsConstructor
public class AdminWechatWebLoginProvider implements LoginProvider {

    private final WechatWebLoginBinding binding;
    private final AdminWechatWebAuthService adminWechatWebAuthService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return AdminAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return WechatWebConstants.OAUTH_PROVIDER;
    }

    @Override
    public String loginPathSegment() {
        if (binding.loginPathSegment() != null && !binding.loginPathSegment().isBlank()) {
            return binding.loginPathSegment();
        }
        return WechatWebConstants.LOGIN_PATH_SEGMENT;
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        WechatWebLoginRequest loginRequest = parseRequest(request);
        if (!StringUtils.hasText(loginRequest.code())) {
            throw new AuthenticationServiceException("微信登录 code 不能为空");
        }
        if (!StringUtils.hasText(loginRequest.state())) {
            throw new AuthenticationServiceException("微信登录 state 不能为空");
        }
        return new WechatWebAuthenticationToken(loginRequest.code().trim(), loginRequest.state().trim());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatWebAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String code = authentication.getPrincipal() == null ? null : authentication.getPrincipal().toString();
        String state = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();
        return adminWechatWebAuthService.loginByWechatWebCode(code, state);
    }

    private WechatWebLoginRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), WechatWebLoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信登录请求解析失败", ex);
        }
    }
}
