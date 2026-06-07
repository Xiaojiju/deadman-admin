package com.mtfm.deadman.component.client.config;

import com.mtfm.deadman.component.client.auth.handler.ClientLoginFailureHandler;
import com.mtfm.deadman.component.client.auth.handler.ClientLoginSuccessHandler;
import com.mtfm.deadman.component.client.auth.handler.ClientSecurityJsonHandlers;
import com.mtfm.deadman.component.client.auth.jwt.ClientJwtAuthenticationFilter;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroup;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager;
import com.mtfm.deadman.security.authentication.provider.LoginProviderSecuritySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 用户端独立 SecurityFilterChain，与管理端 Filter 完全隔离，AuthenticationManager 由 security 统一 Provider 管理器提供。
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
    private final LoginProviderGroupManager loginProviderGroupManager;

    /**
     * 用户端安全过滤链，仅匹配 /client/api/**。
     *
     * @param http HttpSecurity
     * @return 过滤链
     */
    @Bean
    @Order(20)
    SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        LoginProviderGroup clientGroup = loginProviderGroupManager.requireGroup(ClientAuthConstants.LOGIN_GROUP_ID);
        AuthenticationManager clientAuthenticationManager =
                loginProviderGroupManager.requireAuthenticationManager(ClientAuthConstants.LOGIN_GROUP_ID);
        String authBase = clientComponentProperties.getAuth().getBasePath();

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
                    for (var provider : loginProviderGroupManager.listProviders(ClientAuthConstants.LOGIN_GROUP_ID)) {
                        auth.requestMatchers(
                                        HttpMethod.POST, provider.resolveLoginEndpoint(clientGroup))
                                .permitAll();
                    }
                    auth.requestMatchers("/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(clientJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        LoginProviderSecuritySupport.registerProviderLoginFilters(
                http,
                clientGroup,
                loginProviderGroupManager,
                clientAuthenticationManager,
                clientLoginSuccessHandler,
                clientLoginFailureHandler);

        return http.build();
    }
}
