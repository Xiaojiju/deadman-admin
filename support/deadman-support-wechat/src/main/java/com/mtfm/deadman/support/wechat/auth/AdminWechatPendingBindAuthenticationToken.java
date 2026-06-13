package com.mtfm.deadman.support.wechat.auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 管理端微信 OAuth 待绑定认证令牌，登录成功时由待绑定 OAuth 处理器响应临时令牌。
 */
@Getter
public class AdminWechatPendingBindAuthenticationToken extends AbstractAuthenticationToken {

    private final AdminWechatPendingBindPrincipal principal;

    /**
     * 构造已认证的待绑定令牌。
     *
     * @param principal 待绑定主体（含 bindToken 与过期秒数）
     */
    public AdminWechatPendingBindAuthenticationToken(AdminWechatPendingBindPrincipal principal) {
        super(Collections.emptyList());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AdminWechatPendingBindPrincipal getPrincipal() {
        return principal;
    }
}
