package com.mtfm.deadman.component.client.auth.provider;

import com.mtfm.deadman.component.client.spi.ClientLoginProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 将 {@link ClientLoginProvider} 适配为 Spring Security {@link AuthenticationProvider}。
 */
@RequiredArgsConstructor
public class ClientLoginProviderAuthenticationProvider implements AuthenticationProvider {

    private final ClientLoginProvider loginProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return loginProvider.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return loginProvider.supports(authentication);
    }
}
