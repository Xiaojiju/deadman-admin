package com.mtfm.deadman.security.spi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

/**
 * 待绑定 OAuth 登录成功处理器 SPI。
 * <p>
 * 当 OAuth 登录尚未关联本地账号、需返回临时令牌而非 JWT 时，由桥接模块实现并在
 * {@link com.mtfm.deadman.security.authentication.JwtLoginSuccessHandler} 中优先调用。
 */
public interface PendingOAuthBindLoginHandler {

    /**
     * 是否支持处理该认证结果。
     *
     * @param authentication 登录成功后的认证对象
     * @return 是否由本处理器响应
     */
    boolean supports(Authentication authentication);

    /**
     * 写入待绑定 OAuth 登录成功响应。
     *
     * @param request        HTTP 请求
     * @param response       HTTP 响应
     * @param authentication 认证对象
     */
    void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException;
}
