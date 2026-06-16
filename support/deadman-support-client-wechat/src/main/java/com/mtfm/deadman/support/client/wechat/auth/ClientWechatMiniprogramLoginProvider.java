package com.mtfm.deadman.support.client.wechat.auth;

import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.auth.WechatMiniprogramAuthenticationToken;
import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramLoginBinding;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatMiniprogramLoginRequest;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import com.mtfm.deadman.support.client.wechat.service.ClientWechatAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 用户端微信小程序登录 Provider，覆盖插件默认实现：未绑定时返回待绑定临时令牌而非自动开通用户。
 */
@RequiredArgsConstructor
public class ClientWechatMiniprogramLoginProvider implements LoginProvider {

    private final WechatMiniprogramLoginBinding binding;
    private final ClientWechatAuthService clientWechatAuthService;
    private final JsonMapper jsonMapper;

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    @Override
    public String providerId() {
        return WechatMiniprogramConstants.OAUTH_PROVIDER;
    }

    @Override
    public String loginPathSegment() {
        if (binding.loginPathSegment() != null && !binding.loginPathSegment().isBlank()) {
            return binding.loginPathSegment();
        }
        return WechatMiniprogramConstants.LOGIN_PATH_SEGMENT;
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        WechatMiniprogramLoginRequest loginRequest = parseRequest(request);
        if (!StringUtils.hasText(loginRequest.code())) {
            throw new AuthenticationServiceException("微信登录 code 不能为空");
        }
        return new WechatMiniprogramAuthenticationToken(loginRequest.code().trim());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatMiniprogramAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String code = authentication.getPrincipal() == null ? null : authentication.getPrincipal().toString();
        return clientWechatAuthService.loginByWechatCode(code);
    }

    private WechatMiniprogramLoginRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), WechatMiniprogramLoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信登录请求解析失败", ex);
        }
    }
}
