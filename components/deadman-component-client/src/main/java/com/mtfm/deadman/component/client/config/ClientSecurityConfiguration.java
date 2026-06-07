package com.mtfm.deadman.component.client.config;

import com.mtfm.deadman.component.client.auth.filter.ClientProviderLoginFilter;
import com.mtfm.deadman.component.client.auth.handler.ClientLoginFailureHandler;
import com.mtfm.deadman.component.client.auth.handler.ClientLoginSuccessHandler;
import com.mtfm.deadman.component.client.auth.handler.ClientSecurityJsonHandlers;
import com.mtfm.deadman.component.client.auth.jwt.ClientJwtAuthenticationFilter;
import com.mtfm.deadman.component.client.auth.provider.ClientLoginProviderAuthenticationProvider;
import com.mtfm.deadman.component.client.spi.ClientLoginProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * 用户端独立 SecurityFilterChain，与管理端 Filter 完全隔离。
 */
@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ClientSecurityConfiguration {

    private final ClientComponentProperties clientComponentProperties;
    private final ClientJwtAuthenticationFilter clientJwtAuthenticationFilter;
    private final ClientSecurityJsonHandlers clientSecurityJsonHandlers;
    private final ClientLoginSuccessHandler clientLoginSuccessHandler;
    private final ClientLoginFailureHandler clientLoginFailureHandler;
    private final List<ClientLoginProvider> loginProviders;

    /**
     * 用户端独立 AuthenticationManager，仅包含用户端 LoginProvider。
     *
     * @return 认证管理器
     */
    @Bean
    AuthenticationManager clientAuthenticationManager() {
        java.util.List<AuthenticationProvider> providers = loginProviders.stream()
                .<AuthenticationProvider>map(ClientLoginProviderAuthenticationProvider::new)
                .toList();
        return new ProviderManager(providers);
    }

    /**
     * 用户端安全过滤链，仅匹配 /client/api/**。
     *
     * @param http HttpSecurity
     * @return 过滤链
     */
    @Bean
    @Order(20)
    SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http, AuthenticationManager clientAuthenticationManager) throws Exception {
        String authBase = clientComponentProperties.getAuth().getBasePath();
        String loginPrefix = clientComponentProperties.getAuth().getLoginPathPrefix();

        http.securityMatcher("/client/api/**")
                .authenticationManager(clientAuthenticationManager)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(clientSecurityJsonHandlers)
                        .accessDeniedHandler(clientSecurityJsonHandlers))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, authBase + "/register").permitAll();
                    for (ClientLoginProvider provider : loginProviders) {
                        auth.requestMatchers(
                                        HttpMethod.POST, authBase + loginPrefix + "/" + provider.loginPathSegment())
                                .permitAll();
                    }
                    auth.requestMatchers("/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(clientJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        for (ClientLoginProvider provider : loginProviders) {
            String loginEndpoint = authBase + loginPrefix + "/" + provider.loginPathSegment();
            ClientProviderLoginFilter filter = new ClientProviderLoginFilter(
                    provider,
                    loginEndpoint,
                    clientAuthenticationManager,
                    clientLoginSuccessHandler,
                    clientLoginFailureHandler);
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
