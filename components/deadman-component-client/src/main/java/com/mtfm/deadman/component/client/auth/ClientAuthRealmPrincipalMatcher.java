package com.mtfm.deadman.component.client.auth;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.spi.AuthRealmPrincipalMatcher;
import org.springframework.stereotype.Component;

/**
 * C 端认证域 Principal 匹配器。
 */
@Component
public class ClientAuthRealmPrincipalMatcher implements AuthRealmPrincipalMatcher {

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthRealm realm() {
        return AuthRealm.CLIENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Object principal) {
        return principal instanceof ClientLoginUser;
    }
}
