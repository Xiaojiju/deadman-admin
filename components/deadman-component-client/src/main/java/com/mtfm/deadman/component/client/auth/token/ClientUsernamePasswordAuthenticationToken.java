package com.mtfm.deadman.component.client.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 用户端用户名密码认证令牌，与管理端 {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken} 隔离。
 */
public class ClientUsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /**
     * 创建未认证令牌。
     *
     * @param username 用户名
     * @param password 密码
     */
    public ClientUsernamePasswordAuthenticationToken(String username, String password) {
        super(Collections.emptyList());
        this.principal = username;
        this.credentials = password;
        setAuthenticated(false);
    }

    /**
     * 创建已认证令牌。
     *
     * @param principal   用户主体
     * @param authorities 权限
     */
    public ClientUsernamePasswordAuthenticationToken(
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
