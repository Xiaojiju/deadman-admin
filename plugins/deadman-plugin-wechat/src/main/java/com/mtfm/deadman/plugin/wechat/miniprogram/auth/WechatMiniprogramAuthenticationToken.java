package com.mtfm.deadman.plugin.wechat.miniprogram.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 微信小程序登录认证令牌。
 */
public class WechatMiniprogramAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    /**
     * 未认证令牌，principal 为 wx.login 的 code。
     *
     * @param code 临时登录凭证
     */
    public WechatMiniprogramAuthenticationToken(String code) {
        super(Collections.emptyList());
        this.principal = code;
        this.credentials = null;
        setAuthenticated(false);
    }

    /**
     * 已认证令牌。
     *
     * @param principal   用户主体
     * @param authorities 权限
     */
    public WechatMiniprogramAuthenticationToken(
            Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
