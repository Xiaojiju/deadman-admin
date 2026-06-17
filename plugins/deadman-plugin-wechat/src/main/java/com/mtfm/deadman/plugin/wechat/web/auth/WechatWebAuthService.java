package com.mtfm.deadman.plugin.wechat.web.auth;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatWebLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.security.spi.OAuthLoginUserService;
import com.mtfm.deadman.security.spi.OAuthLoginUserServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 微信网页扫码登录认证核心服务：通过 {@link WechatLoginService} 解析凭证并注入 OAuth 用户体系。
 */
@Service
@RequiredArgsConstructor
public class WechatWebAuthService {

    private final WechatLoginService wechatLoginService;
    private final OAuthLoginUserServiceManager oauthLoginUserServiceManager;

    /**
     * 使用授权 code 完成认证并注入指定用户体系。
     *
     * @param code         微信授权临时凭证
     * @param state        OAuth state
     * @param loginGroupId 目标用户体系组标识
     * @return 已认证令牌
     */
    public Authentication authenticate(String code, String state, String loginGroupId) throws AuthenticationException {
        if (!StringUtils.hasText(code)) {
            throw new AuthenticationServiceException("微信登录 code 不能为空");
        }
        WechatLoginSession session = wechatLoginService.resolve(new WechatWebLoginCredential(code.trim(), state));
        OAuthLoginUserService oauthLoginUserService = oauthLoginUserServiceManager.require(loginGroupId);
        Authentication resolved = oauthLoginUserService.resolveOAuthLogin(new OAuthLoginUserService.OAuthLoginRequest(
                session.oauthProvider(),
                session.oauthSubject(),
                session.openid(),
                session.displayNickname(),
                session.avatarUrl()));
        return new WechatWebAuthenticationToken(resolved.getPrincipal(), resolved.getAuthorities());
    }
}
