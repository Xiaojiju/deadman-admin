package com.mtfm.deadman.plugin.wechat.web.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 微信网页扫码登录认证令牌。
 */
public class WechatWebAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    /**
     * 未认证令牌，principal 为授权 code，credentials 为 OAuth state。
     *
     * @param code  授权临时凭证
     * @param state OAuth state
     */
    public WechatWebAuthenticationToken(String code, String state) {
        super(Collections.emptyList());
        this.principal = code;
        this.credentials = state;
        setAuthenticated(false);
    }

    /**
     * 已认证令牌。
     *
     * @param principal   用户主体
     * @param authorities 权限
     */
    public WechatWebAuthenticationToken(
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
