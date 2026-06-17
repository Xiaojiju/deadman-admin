package com.mtfm.deadman.support.client.wechat.auth;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import com.mtfm.deadman.support.client.wechat.dto.ClientWechatBindRequest;
import com.mtfm.deadman.support.client.wechat.service.ClientWechatWebAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 用户端微信网页扫码 OAuth 绑定登录 Provider：校验用户名密码并将 openid 绑定到用户端用户。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-web", name = "enabled", havingValue = "true")
public class ClientWechatWebBindLoginProvider implements LoginProvider {

    /** 绑定登录路径 */
    public static final String BIND_LOGIN_ENDPOINT = "/client/api/auth/wechat-web/bind";

    private final ClientWechatWebAuthService clientWechatWebAuthService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return "wechat-web-bind";
    }

    @Override
    public String customLoginEndpoint() {
        return BIND_LOGIN_ENDPOINT;
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        ClientWechatBindRequest bindRequest = parseRequest(request);
        if (!StringUtils.hasText(bindRequest.bindToken())
                || !StringUtils.hasText(bindRequest.username())
                || !StringUtils.hasText(bindRequest.password())) {
            throw new AuthenticationServiceException("绑定令牌、用户名或密码不能为空");
        }
        return new ClientWechatBindAuthenticationToken(bindRequest);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientWechatBindAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ClientWechatBindAuthenticationToken bindToken = (ClientWechatBindAuthenticationToken) authentication;
        ClientWechatBindRequest request = bindToken.getBindRequest();
        return clientWechatWebAuthService.bindAndAuthenticate(
                request.bindToken(), request.username(), request.password());
    }

    private ClientWechatBindRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), ClientWechatBindRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信绑定请求解析失败", ex);
        }
    }
}
