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
import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatMiniprogramLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.plugin.wechat.login.session.WechatMiniprogramLoginSession;
import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.support.client.wechat.auth.ClientWechatPendingBindAuthenticationToken;
import com.mtfm.deadman.support.client.wechat.auth.ClientWechatPendingBindPrincipal;
import com.mtfm.deadman.support.client.wechat.vo.ClientWechatBindingStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * 用户端微信 OAuth 登录与绑定业务服务。
 */
@Service
@RequiredArgsConstructor
public class ClientWechatAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
    private static final int PROFILE_USERNAME_MAX_RETRY = 8;

    private final WechatLoginService wechatLoginService;
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
        WechatLoginSession session = wechatLoginService.resolve(new WechatMiniprogramLoginCredential(code.trim()));
        if (!(session instanceof WechatMiniprogramLoginSession miniprogramSession)) {
            throw new BadCredentialsException("微信登录会话类型不匹配");
        }
        ClientUserAccount oauthAccount = clientUserAccountService.findByOAuth(
                session.oauthProvider(), session.openid());
        if (oauthAccount != null) {
            return authenticateBoundUser(oauthAccount);
        }
        String bindToken = bindTokenStore.store(new ClientWechatPendingSession(
                miniprogramSession.openid(), miniprogramSession.sessionKey(), miniprogramSession.unionid()));
        long expiresIn = bindTokenStore.bindTokenExpiresInSeconds();
        return new ClientWechatPendingBindAuthenticationToken(
                new ClientWechatPendingBindPrincipal(bindToken, expiresIn));
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
     * @param avatarFileId 头像文件 ID，可为空；微信 CDN 外链不可写入，仅平台文件 ID
     * @param phone        手机号，可为空
     * @param inviteCode   推广码，可为空
     * @return 已认证的用户端登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Authentication registerAndBind(String bindToken, String username, String password, String nickname,
            Long avatarFileId, String phone, String inviteCode) {
        ClientWechatPendingSession session = consumeBindSession(bindToken);
        return registerAndBindWithSession(
                session, username, password, nickname, avatarFileId, phone, inviteCode);
    }

    /**
     * 完善资料模式：消费 bindToken，服务端生成账密后复用 {@link #registerAndBind} 建号+绑定链路。
     *
     * @param bindToken    待绑定临时令牌
     * @param nickname     昵称（必填）
     * @param avatarFileId 头像文件 ID，可为空
     * @param phone        手机号（必填）
     * @param inviteCode   推广码，可为空
     * @return 已认证的用户端登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Authentication registerAndBindByProfile(
            String bindToken, String nickname, Long avatarFileId, String phone, String inviteCode) {
        if (!StringUtils.hasText(nickname)) {
            throw new BadCredentialsException("昵称不能为空");
        }
        if (!StringUtils.hasText(phone)) {
            throw new BadCredentialsException("手机号不能为空");
        }
        ClientWechatPendingSession session = consumeBindSession(bindToken);
        String username = allocateProfileUsername(session.openid());
        String password = generateRandomPassword(16);
        return registerAndBindWithSession(
                session, username, password, nickname.trim(), avatarFileId, phone.trim(), inviteCode);
    }

    private Authentication registerAndBindWithSession(
            ClientWechatPendingSession session,
            String username,
            String password,
            String nickname,
            Long avatarFileId,
            String phone,
            String inviteCode) {
        ClientUserBase userBase;
        try {
            userBase = clientAuthCredentialsService.registerUser(
                    new ClientRegisterRequest(username.trim(), password, nickname, avatarFileId, phone, inviteCode));
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

    private String allocateProfileUsername(String openid) {
        String suffix = normalizeOpenidSuffix(openid);
        for (int i = 0; i < PROFILE_USERNAME_MAX_RETRY; i++) {
            String candidate = "wx_" + suffix + "_" + randomAlnum(4);
            if (!clientUserAccountService.existsUsername(candidate)) {
                return candidate;
            }
        }
        // 极端冲突：再加时间戳
        String fallback = "wx_" + suffix + "_" + Long.toString(System.currentTimeMillis(), 36);
        if (fallback.length() > 64) {
            fallback = fallback.substring(0, 64);
        }
        if (clientUserAccountService.existsUsername(fallback)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "无法分配登录用户名，请重试");
        }
        return fallback;
    }

    private static String normalizeOpenidSuffix(String openid) {
        String raw = StringUtils.hasText(openid) ? openid.trim() : "user";
        String cleaned = raw.replaceAll("[^A-Za-z0-9]", "");
        if (!StringUtils.hasText(cleaned)) {
            cleaned = Integer.toHexString(raw.hashCode());
        }
        if (cleaned.length() > 8) {
            cleaned = cleaned.substring(cleaned.length() - 8);
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private static String generateRandomPassword(int length) {
        int len = Math.max(8, length);
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = PASSWORD_ALPHABET[SECURE_RANDOM.nextInt(PASSWORD_ALPHABET.length)];
        }
        return new String(buf);
    }

    private static String randomAlnum(int length) {
        final char[] alphabet = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = alphabet[SECURE_RANDOM.nextInt(alphabet.length)];
        }
        return new String(buf);
    }

    /**
     * 已登录用户使用 wx.login code 绑定当前微信小程序 openid。
     * <p>
     * 不需要 bindToken / 密码；身份由 CLIENT JWT 确定，微信身份由 code2session 确定。
     * 当前用户已绑定时幂等返回；openid 已被其他用户占用时抛出 {@link ResultCode#OAUTH_ALREADY_BOUND}。
     *
     * @param userId 当前登录用户 ID
     * @param code   wx.login 临时凭证
     * @return 绑定状态（成功后 bound=true）
     */
    @Transactional(rollbackFor = Exception.class)
    public ClientWechatBindingStatusVO bindMiniprogramForCurrentUser(Long userId, String code) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
        }
        if (clientUserAccountService.findMiniprogramOpenid(userId).isPresent()) {
            return new ClientWechatBindingStatusVO(true);
        }
        WechatLoginSession session;
        try {
            session = wechatLoginService.resolve(new WechatMiniprogramLoginCredential(code.trim()));
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信授权失败，请重试", ex);
        }
        if (!(session instanceof WechatMiniprogramLoginSession)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录会话类型不匹配");
        }
        clientUserAccountService.bindOAuth(
                userId, WechatMiniprogramConstants.OAUTH_PROVIDER, session.openid());
        return new ClientWechatBindingStatusVO(true);
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
