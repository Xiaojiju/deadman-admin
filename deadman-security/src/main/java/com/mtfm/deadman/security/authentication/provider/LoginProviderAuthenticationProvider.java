package com.mtfm.deadman.security.authentication.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 将 {@link LoginProvider} 适配为 Spring Security {@link AuthenticationProvider}。
 */
@RequiredArgsConstructor
public class LoginProviderAuthenticationProvider implements AuthenticationProvider {

    private final LoginProvider loginProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return loginProvider.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return loginProvider.supports(authentication);
    }
}
