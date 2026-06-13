package com.mtfm.deadman.support.wechat.auth;

import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.support.wechat.dto.AdminWechatBindRequest;
import com.mtfm.deadman.support.wechat.service.AdminWechatAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 管理端微信 OAuth 绑定登录 Provider：校验用户名密码并将 openid 绑定到管理端用户。
 */
@Component
@RequiredArgsConstructor
public class AdminWechatBindLoginProvider implements LoginProvider {

    /** 绑定登录路径 */
    public static final String BIND_LOGIN_ENDPOINT = "/api/auth/wechat-miniprogram/bind";

    private final AdminWechatAuthService adminWechatAuthService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return AdminAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return "wechat-miniprogram-bind";
    }

    @Override
    public String customLoginEndpoint() {
        return BIND_LOGIN_ENDPOINT;
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        AdminWechatBindRequest bindRequest = parseRequest(request);
        if (!StringUtils.hasText(bindRequest.bindToken())
                || !StringUtils.hasText(bindRequest.username())
                || !StringUtils.hasText(bindRequest.password())) {
            throw new AuthenticationServiceException("绑定令牌、用户名或密码不能为空");
        }
        return new AdminWechatBindAuthenticationToken(bindRequest);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AdminWechatBindAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AdminWechatBindAuthenticationToken bindToken = (AdminWechatBindAuthenticationToken) authentication;
        AdminWechatBindRequest request = bindToken.getBindRequest();
        return adminWechatAuthService.bindAndAuthenticate(
                request.bindToken(), request.username(), request.password());
    }

    private AdminWechatBindRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), AdminWechatBindRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信绑定请求解析失败", ex);
        }
    }
}
