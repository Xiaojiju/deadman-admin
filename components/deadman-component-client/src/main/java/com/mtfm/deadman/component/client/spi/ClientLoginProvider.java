package com.mtfm.deadman.component.client.spi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 用户端登录 Provider SPI，每种登录方式实现一个 Bean（如 password、wechat）。
 * <p>
 * 各 Provider 拥有独立 endpoint，由组件统一注册 Filter。
 */
public interface ClientLoginProvider {

    /**
     * 提供商标识，如 password、wechat。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 登录路径段，完整路径为 {@code {authBasePath}/login/{segment}}。
     *
     * @return 路径段，默认与 providerId 相同
     */
    default String loginPathSegment() {
        return providerId();
    }

    /**
     * 从 HTTP 请求构建未认证的 Authentication。
     *
     * @param request 登录请求
     * @return 未认证令牌
     */
    Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException;

    /**
     * 是否支持该 Authentication 类型。
     *
     * @param authentication 认证类型
     * @return 是否支持
     */
    boolean supports(Class<?> authentication);

    /**
     * 执行认证。
     *
     * @param authentication 认证令牌
     * @return 已认证令牌
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException;
}
