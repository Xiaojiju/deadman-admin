package com.mtfm.deadman.security.authentication.provider;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 登录 Provider SPI，每种登录方式实现一个 Bean（如 password、wechat-miniprogram）。
 * <p>
 * 通过 {@link #loginGroupId()} 归属到特定用户体系，由 {@link LoginProviderGroupManager} 统一聚合。
 */
public interface LoginProvider {

    /**
     * 所属登录 Provider 组标识，如 admin、client。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 提供商标识，如 password、wechat-miniprogram。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 登录路径段，默认与 providerId 相同。
     *
     * @return 路径段
     */
    default String loginPathSegment() {
        return providerId();
    }

    /**
     * 自定义完整登录路径；非空时优先于组内默认拼接规则。
     *
     * @return 完整登录路径，默认 null 表示按组规则拼接
     */
    default String customLoginEndpoint() {
        return null;
    }

    /**
     * 解析完整登录 endpoint。
     *
     * @param group 所属 Provider 组
     * @return 完整登录路径
     */
    default String resolveLoginEndpoint(LoginProviderGroup group) {
        String custom = customLoginEndpoint();
        if (custom != null && !custom.isBlank()) {
            return custom;
        }
        String prefix = group.loginPathPrefix();
        if (prefix == null || prefix.isBlank()) {
            return group.authBasePath() + "/" + loginPathSegment();
        }
        return group.authBasePath() + prefix + "/" + loginPathSegment();
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
