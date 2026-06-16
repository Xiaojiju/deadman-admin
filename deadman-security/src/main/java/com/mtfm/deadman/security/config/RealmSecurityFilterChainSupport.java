package com.mtfm.deadman.security.config;

import com.mtfm.deadman.security.authentication.provider.LoginProviderGroup;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager;
import com.mtfm.deadman.security.authentication.provider.LoginProviderSecuritySupport;
import com.mtfm.deadman.security.jwt.AuthTokenRefreshFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 多 realm SecurityFilterChain 公共配置辅助。
 */
public final class RealmSecurityFilterChainSupport {

    private RealmSecurityFilterChainSupport() {
    }

    /**
     * 应用无状态 JWT 链路的通用安全配置。
     *
     * @param http                  HttpSecurity
     * @param authenticationManager 认证管理器
     * @param entryPoint            401 处理器
     * @param accessDeniedHandler   403 处理器
     */
    public static void applyStatelessJwtDefaults(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            org.springframework.security.web.AuthenticationEntryPoint entryPoint,
            org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authenticationManager)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler));
    }

    /**
     * 注册 Refresh Filter 与 JWT 认证 Filter（Refresh 在前）。
     *
     * @param http           HttpSecurity
     * @param refreshFilter  刷新 Filter
     * @param jwtAuthFilter  Access 认证 Filter
     */
    public static void registerJwtFilters(
            HttpSecurity http, AuthTokenRefreshFilter refreshFilter, OncePerRequestFilter jwtAuthFilter) {
        http.addFilterBefore(refreshFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 放行指定组下所有 Provider 登录端点。
     *
     * @param auth        授权配置
     * @param groupManager Provider 组管理器
     * @param groupId     组标识
     * @param group       Provider 组
     */
    public static void permitProviderLoginEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            LoginProviderGroupManager groupManager,
            String groupId,
            LoginProviderGroup group) {
        for (var provider : groupManager.listProviders(groupId)) {
            auth.requestMatchers(HttpMethod.POST, provider.resolveLoginEndpoint(group)).permitAll();
        }
    }

    /**
     * 注册 Provider 登录 Filter 并完成 FilterChain 构建。
     *
     * @param http                  HttpSecurity
     * @param group                 Provider 组
     * @param groupManager          Provider 组管理器
     * @param authenticationManager 认证管理器
     * @param successHandler        登录成功处理器
     * @param failureHandler        登录失败处理器
     * @return 过滤链
     */
    public static SecurityFilterChain buildWithProviderLoginFilters(
            HttpSecurity http,
            LoginProviderGroup group,
            LoginProviderGroupManager groupManager,
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler)
            throws Exception {
        LoginProviderSecuritySupport.registerProviderLoginFilters(
                http, group, groupManager, authenticationManager, successHandler, failureHandler);
        return http.build();
    }
}
