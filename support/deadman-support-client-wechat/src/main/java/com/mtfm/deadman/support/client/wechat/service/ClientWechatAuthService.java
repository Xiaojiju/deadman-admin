package com.mtfm.deadman.support.client.wechat.service;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.dto.ClientRegisterRequest;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientAuthCredentialsService;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.component.client.service.ClientUserPasswordService;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatCode2SessionResult;
import com.mtfm.deadman.support.client.wechat.auth.ClientWechatPendingBindAuthenticationToken;
import com.mtfm.deadman.support.client.wechat.auth.ClientWechatPendingBindPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户端微信 OAuth 登录与绑定业务服务。
 */
@Service
@RequiredArgsConstructor
public class ClientWechatAuthService {

    private final WechatApiClient wechatApiClient;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserService clientUserService;
    private final ClientUserPasswordService clientUserPasswordService;
    private final ClientAuthCredentialsService clientAuthCredentialsService;
    private final ClientWechatBindTokenStore bindTokenStore;

    /**
     * 使用 wx.login code 完成用户端微信登录：已绑定则直接认证，未绑定则返回待绑定令牌。
     *
     * @param code 微信临时登录凭证
     * @return 已认证令牌或待绑定令牌
     */
    public Authentication loginByWechatCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BadCredentialsException("微信登录 code 不能为空");
        }
        WechatCode2SessionResult session = wechatApiClient.code2Session(code.trim());
        ClientUserAccount oauthAccount = clientUserAccountService.findByOAuth(
                WechatMiniprogramConstants.OAUTH_PROVIDER, session.openid());
        if (oauthAccount != null) {
            return authenticateBoundUser(oauthAccount);
        }
        String bindToken = bindTokenStore.store(new ClientWechatPendingSession(
                session.openid(), session.sessionKey(), session.unionid()));
        long expiresIn = bindTokenStore.bindTokenExpiresInSeconds();
        return new ClientWechatPendingBindAuthenticationToken(new ClientWechatPendingBindPrincipal(bindToken, expiresIn));
    }

    /**
     * 使用用户名密码完成二次认证，并将微信 openid 绑定到该用户端用户。
     *
     * @param bindToken 待绑定临时令牌
     * @param username  用户端用户名
     * @param password  用户端密码
     * @return 已认证的用户端登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Authentication bindAndAuthenticate(String bindToken, String username, String password) {
        ClientWechatPendingSession session = consumeBindSession(bindToken);
        Authentication passwordAuth = authenticateByPassword(username, password);
        ClientLoginUser loginUser = (ClientLoginUser) passwordAuth.getPrincipal();
        clientUserAccountService.bindOAuth(
                loginUser.getUserId(), WechatMiniprogramConstants.OAUTH_PROVIDER, session.openid());
        return passwordAuth;
    }

    /**
     * 使用 bindToken 注册新用户、绑定微信 openid 并返回已认证令牌（注册成功后直接登录）。
     *
     * @param bindToken 待绑定临时令牌
     * @param username  登录用户名
     * @param password  密码
     * @param nickname  昵称，可为空
     * @return 已认证的用户端登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Authentication registerAndBind(String bindToken, String username, String password, String nickname) {
        ClientWechatPendingSession session = consumeBindSession(bindToken);
        ClientUserBase userBase;
        try {
            userBase = clientAuthCredentialsService.registerUser(
                    new ClientRegisterRequest(username.trim(), password, nickname));
        } catch (BusinessException ex) {
            if (ex.getCode() == ResultCode.ACCOUNT_EXISTS.getCode()) {
                throw new BadCredentialsException("用户名已存在", ex);
            }
            throw ex;
        }
        clientUserAccountService.bindOAuth(
                userBase.getId(), WechatMiniprogramConstants.OAUTH_PROVIDER, session.openid());
        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, username.trim());
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    /**
     * 校验用户端用户名密码并返回已认证令牌。
     *
     * @param username 用户名
     * @param password 密码
     * @return 已认证令牌
     */
    public Authentication authenticateByPassword(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        ClientUserAccount account = clientUserAccountService.findByUsername(username.trim());
        if (account == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        ClientUserBase userBase = clientUserService.requireById(account.getUserId());
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }
        try {
            if (!clientUserPasswordService.matches(userBase.getId(), password)) {
                throw new BadCredentialsException("用户名或密码错误");
            }
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("用户名或密码错误", ex);
        }
        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, username.trim());
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    private Authentication authenticateBoundUser(ClientUserAccount oauthAccount) {
        ClientUserBase userBase = clientUserService.requireById(oauthAccount.getUserId());
        if (userBase.getStatus() == null || userBase.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new DisabledException("用户已禁用");
        }
        ClientLoginUser loginUser = clientUserService.buildLoginUser(userBase, oauthAccount.getAccountIdentifier());
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    private ClientWechatPendingSession consumeBindSession(String bindToken) {
        try {
            return bindTokenStore.consume(bindToken);
        } catch (BusinessException ex) {
            if (ex.getCode() == ResultCode.WECHAT_BIND_TOKEN_INVALID.getCode()) {
                throw new BadCredentialsException(ex.getMessage(), ex);
            }
            throw ex;
        }
    }
}
