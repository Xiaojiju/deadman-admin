package com.mtfm.deadman.security.config;

import com.mtfm.deadman.security.JwtAuthenticationFilter;
import com.mtfm.deadman.security.SecurityJsonHandlers;
import com.mtfm.deadman.security.authentication.JwtLoginFailureHandler;
import com.mtfm.deadman.security.authentication.JwtLoginSuccessHandler;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroup;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.jwt.AuthTokenRefreshFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security：JWT 无状态、统一 Provider 登录 Filter、统一 JSON 401/403。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthTokenRefreshFilter authTokenRefreshFilter;
    private final SecurityJsonHandlers securityJsonHandlers;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;
    private final LoginProviderGroupManager loginProviderGroupManager;

    /**
     * 管理端安全过滤链，匹配 /api/**。
     *
     * @param http HttpSecurity
     * @return 过滤链
     */
    @Bean
    @Order(100)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LoginProviderGroup adminGroup = loginProviderGroupManager.requireGroup(AdminAuthConstants.LOGIN_GROUP_ID);
        AuthenticationManager adminAuthenticationManager = loginProviderGroupManager
                .requireAuthenticationManager(AdminAuthConstants.LOGIN_GROUP_ID);

        http.securityMatcher("/api/**");
        RealmSecurityFilterChainSupport.applyStatelessJwtDefaults(
                http, adminAuthenticationManager, securityJsonHandlers, securityJsonHandlers);
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(HttpMethod.GET, "/api/components").permitAll();
            auth.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
            auth.requestMatchers(HttpMethod.POST, AdminAuthConstants.REFRESH_TOKEN_PATH).permitAll();
            RealmSecurityFilterChainSupport.permitProviderLoginEndpoints(
                    auth, loginProviderGroupManager, AdminAuthConstants.LOGIN_GROUP_ID, adminGroup);
            auth.requestMatchers("/error").permitAll();
            auth.anyRequest().authenticated();
        });
        RealmSecurityFilterChainSupport.registerJwtFilters(http, authTokenRefreshFilter, jwtAuthenticationFilter);
        return RealmSecurityFilterChainSupport.buildWithProviderLoginFilters(
                http,
                adminGroup,
                loginProviderGroupManager,
                adminAuthenticationManager,
                jwtLoginSuccessHandler,
                jwtLoginFailureHandler);
    }
}
