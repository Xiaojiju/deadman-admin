package com.mtfm.deadman.plugin.smslogin.auth;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * 手机验证码认证令牌。
 */
public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /**
     * 未认证令牌。
     *
     * @param phone 手机号
     * @param code 验证码
     */
    public SmsCodeAuthenticationToken(String phone, String code) {
        super(Collections.emptyList());
        this.principal = phone;
        this.credentials = code;
        setAuthenticated(false);
    }

    /**
     * 已认证令牌。
     *
     * @param principal 登录用户
     * @param authorities 权限
     */
    public SmsCodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    /**
     * 验证码。
     *
     * @return 凭证
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * 手机号或登录用户。
     *
     * @return 主体
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }
}
