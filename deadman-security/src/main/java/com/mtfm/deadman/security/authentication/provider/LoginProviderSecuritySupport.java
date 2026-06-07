package com.mtfm.deadman.security.authentication.provider;

import com.mtfm.deadman.security.authentication.filter.ProviderLoginFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 登录 Provider 组 Security 配置辅助，统一注册 Provider 专属 Filter。
 */
public final class LoginProviderSecuritySupport {

    private LoginProviderSecuritySupport() {
    }

    /**
     * 为指定 Provider 组注册登录 Filter。
     *
     * @param http                  HttpSecurity
     * @param group                 Provider 组
     * @param groupManager          Provider 组管理器
     * @param authenticationManager 该组的 AuthenticationManager
     * @param successHandler        登录成功处理器
     * @param failureHandler        登录失败处理器
     */
    public static void registerProviderLoginFilters(
            HttpSecurity http,
            LoginProviderGroup group,
            LoginProviderGroupManager groupManager,
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler)
            throws Exception {
        for (LoginProvider provider : groupManager.listProviders(group.groupId())) {
            String loginEndpoint = provider.resolveLoginEndpoint(group);
            ProviderLoginFilter filter = new ProviderLoginFilter(
                    provider, loginEndpoint, authenticationManager, successHandler, failureHandler);
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }
    }
}
