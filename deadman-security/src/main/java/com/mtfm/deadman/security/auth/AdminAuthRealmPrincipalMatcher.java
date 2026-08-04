package com.mtfm.deadman.security.auth;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.spi.AuthRealmPrincipalMatcher;
import com.mtfm.deadman.security.LoginUser;
import org.springframework.stereotype.Component;

/**
 * 管理端认证域 Principal 匹配器。
 */
@Component
public class AdminAuthRealmPrincipalMatcher implements AuthRealmPrincipalMatcher {

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthRealm realm() {
        return AuthRealm.ADMIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Object principal) {
        return principal instanceof LoginUser;
    }
}
