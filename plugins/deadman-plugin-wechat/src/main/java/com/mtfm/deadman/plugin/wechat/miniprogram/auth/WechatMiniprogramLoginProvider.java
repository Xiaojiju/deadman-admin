package com.mtfm.deadman.plugin.wechat.miniprogram.auth;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.spi.ClientLoginProvider;
import com.mtfm.deadman.component.client.spi.ClientUserProvisioner;
import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatCode2SessionResult;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatMiniprogramLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 微信小程序登录 Provider：code2session 后创建或关联用户端用户。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-miniprogram", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WechatMiniprogramLoginProvider implements ClientLoginProvider {

    private final WechatApiClient wechatApiClient;
    private final ClientUserProvisioner clientUserProvisioner;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserService clientUserService;
    private final JsonMapper jsonMapper;

    @Override
    public String providerId() {
        return WechatMiniprogramConstants.OAUTH_PROVIDER;
    }

    @Override
    public String loginPathSegment() {
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
        if (!StringUtils.hasText(code)) {
            throw new AuthenticationServiceException("微信登录 code 不能为空");
        }

        WechatCode2SessionResult session = wechatApiClient.code2Session(code);
        ClientUserAccount account = clientUserAccountService.findByOAuth(WechatMiniprogramConstants.OAUTH_PROVIDER,
                session.openid());
        ClientUserBase userBase;
        if (account != null) {
            userBase = clientUserService.requireById(account.getUserId());
        } else {
            userBase = clientUserProvisioner.provisionOAuthUser(new ClientUserProvisioner.ClientUserProvisionRequest(
                    WechatMiniprogramConstants.OAUTH_PROVIDER,
                    session.openid(),
                    session.openid(),
                    "微信用户",
                    null));
        }

        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }

        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, session.openid());
        return new WechatMiniprogramAuthenticationToken(loginUser, loginUser.getAuthorities());
    }

    private WechatMiniprogramLoginRequest parseRequest(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), WechatMiniprogramLoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("微信登录请求解析失败", ex);
        }
    }
}
