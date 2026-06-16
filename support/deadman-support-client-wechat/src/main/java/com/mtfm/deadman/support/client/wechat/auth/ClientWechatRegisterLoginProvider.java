package com.mtfm.deadman.support.client.wechat.auth;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import com.mtfm.deadman.support.client.wechat.dto.ClientWechatRegisterRequest;
import com.mtfm.deadman.support.client.wechat.service.ClientWechatAuthService;
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
 * 用户端微信 OAuth 绑定注册 Provider：携带 bindToken 注册新用户并完成 openid 绑定，成功后签发 JWT。
 */
@Component
@RequiredArgsConstructor
public class ClientWechatRegisterLoginProvider implements LoginProvider {

    /** 绑定注册路径 */
    public static final String REGISTER_LOGIN_ENDPOINT = "/client/api/auth/wechat-miniprogram/register";

    private final ClientWechatAuthService clientWechatAuthService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return "wechat-miniprogram-register";
    }

    @Override
    public String customLoginEndpoint() {
        return REGISTER_LOGIN_ENDPOINT;
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        ClientWechatRegisterRequest registerRequest = parseRequest(request);
        if (!StringUtils.hasText(registerRequest.bindToken())
                || !StringUtils.hasText(registerRequest.username())
                || !StringUtils.hasText(registerRequest.password())) {
            throw new AuthenticationServiceException("绑定令牌、用户名或密码不能为空");
        }
        return new ClientWechatRegisterAuthenticationToken(registerRequest);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientWechatRegisterAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ClientWechatRegisterAuthenticationToken registerToken = (ClientWechatRegisterAuthenticationToken) authentication;
        ClientWechatRegisterRequest request = registerToken.getRegisterRequest();
        return clientWechatAuthService.registerAndBind(
                request.bindToken(), request.username(), request.password(), request.nickname());
    }

    private ClientWechatRegisterRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), ClientWechatRegisterRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信绑定注册请求解析失败", ex);
        }
    }
}
