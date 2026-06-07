package com.mtfm.deadman.component.client.spi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;

/**
 * 用户端登录失败回调 SPI，插件可注册统一失败处理逻辑（如审计、埋点）。
 */
public interface ClientLoginFailureCallback {

    /**
     * 登录失败时回调。
     *
     * @param request   请求
     * @param response  响应
     * @param providerId 登录提供商标识
     * @param exception 认证异常
     */
    void onLoginFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            String providerId,
            AuthenticationException exception);
}
