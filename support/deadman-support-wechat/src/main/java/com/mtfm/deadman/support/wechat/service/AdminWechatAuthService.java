package com.mtfm.deadman.support.wechat.service;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatMiniprogramLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.plugin.wechat.login.session.WechatMiniprogramLoginSession;
import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.service.AuthPermissionService;
import com.mtfm.deadman.support.wechat.auth.AdminWechatPendingBindAuthenticationToken;
import com.mtfm.deadman.support.wechat.auth.AdminWechatPendingBindPrincipal;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserAccountService;
import com.mtfm.deadman.system.service.UserPasswordService;
import com.mtfm.deadman.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 管理端微信 OAuth 登录与绑定业务服务。
 */
@Service
@RequiredArgsConstructor
public class AdminWechatAuthService {

    private final WechatLoginService wechatLoginService;
    private final UserAccountService userAccountService;
    private final UserService userService;
    private final UserPasswordService userPasswordService;
    private final AuthPermissionService authPermissionService;
    private final AdminWechatBindTokenStore bindTokenStore;

    /**
     * 使用 wx.login code 完成管理端微信登录：已绑定则直接认证，未绑定则返回待绑定令牌。
     *
     * @param code 微信临时登录凭证
     * @return 已认证令牌或待绑定令牌
     */
    public Authentication loginByWechatCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BadCredentialsException("微信登录 code 不能为空");
        }
        WechatLoginSession session = wechatLoginService.resolve(new WechatMiniprogramLoginCredential(code.trim()));
        if (!(session instanceof WechatMiniprogramLoginSession miniprogramSession)) {
            throw new BadCredentialsException("微信登录会话类型不匹配");
        }
        UserAccount oauthAccount = userAccountService.findByOAuth(
                session.oauthProvider(), session.openid());
        if (oauthAccount != null) {
            return authenticateBoundUser(oauthAccount);
        }
        String bindToken = bindTokenStore.store(new AdminWechatPendingSession(
                miniprogramSession.openid(), miniprogramSession.sessionKey(), miniprogramSession.unionid()));
        long expiresIn = bindTokenStore.bindTokenExpiresInSeconds();
        return new AdminWechatPendingBindAuthenticationToken(new AdminWechatPendingBindPrincipal(bindToken, expiresIn));
    }

    /**
     * 使用用户名密码完成二次认证，并将微信 openid 绑定到该管理端用户。
     *
     * @param bindToken 待绑定临时令牌
     * @param username  管理端用户名
     * @param password  管理端密码
     * @return 已认证的管理端登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Authentication bindAndAuthenticate(String bindToken, String username, String password) {
        AdminWechatPendingSession session;
        try {
            session = bindTokenStore.consume(bindToken);
        } catch (BusinessException ex) {
            if (ex.getCode() == ResultCode.WECHAT_BIND_TOKEN_INVALID.getCode()) {
                throw new BadCredentialsException(ex.getMessage(), ex);
            }
            throw ex;
        }
        Authentication passwordAuth = authenticateByPassword(username, password);
        LoginUser loginUser = (LoginUser) passwordAuth.getPrincipal();
        userAccountService.bindOAuth(
                loginUser.getUserId(), WechatMiniprogramConstants.OAUTH_PROVIDER, session.openid());
        return passwordAuth;
    }

    /**
     * 校验管理端用户名密码并返回已认证令牌。
     *
     * @param username 用户名
     * @param password 密码
     * @return 已认证令牌
     */
    public Authentication authenticateByPassword(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        UserAccount account = userAccountService.findByUsername(username.trim());
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
            if (!userPasswordService.matches(userBase.getId(), password)) {
                throw new BadCredentialsException("用户名或密码错误");
            }
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("用户名或密码错误", ex);
        }
        LoginUser loginUser = authPermissionService.buildLoginUser(userBase);
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    private Authentication authenticateBoundUser(UserAccount oauthAccount) {
        UserBase userBase = userService.getById(oauthAccount.getUserId());
        if (userBase == null) {
            throw new BadCredentialsException("绑定的管理端用户不存在");
        }
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }
        LoginUser loginUser = authPermissionService.buildLoginUser(userBase);
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }
}
