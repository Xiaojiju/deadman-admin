package com.mtfm.deadman.plugin.wechat.miniprogram.auth;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatMiniprogramLoginCredential;
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
 * 微信小程序认证核心服务：通过 {@link WechatLoginService} 解析凭证并注入 OAuth 用户体系。
 */
@Service
@RequiredArgsConstructor
public class WechatMiniprogramAuthService {

    private final WechatLoginService wechatLoginService;
    private final OAuthLoginUserServiceManager oauthLoginUserServiceManager;

    /**
     * 使用 wx.login code 完成认证并注入指定用户体系。
     *
     * @param code         微信临时登录凭证
     * @param loginGroupId 目标用户体系组标识
     * @return 已认证令牌
     */
    public Authentication authenticate(String code, String loginGroupId) throws AuthenticationException {
        if (!StringUtils.hasText(code)) {
            throw new AuthenticationServiceException("微信登录 code 不能为空");
        }
        WechatLoginSession session = wechatLoginService.resolve(new WechatMiniprogramLoginCredential(code.trim()));
        OAuthLoginUserService oauthLoginUserService = oauthLoginUserServiceManager.require(loginGroupId);
        Authentication resolved = oauthLoginUserService.resolveOAuthLogin(new OAuthLoginUserService.OAuthLoginRequest(
                session.oauthProvider(),
                session.oauthSubject(),
                session.openid(),
                session.displayNickname(),
                session.avatarUrl()));
        return new WechatMiniprogramAuthenticationToken(resolved.getPrincipal(), resolved.getAuthorities());
    }
}
