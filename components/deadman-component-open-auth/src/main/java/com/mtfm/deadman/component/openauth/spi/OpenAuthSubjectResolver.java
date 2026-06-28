package com.mtfm.deadman.component.openauth.spi;

import org.springframework.security.core.Authentication;

/**
 * 从 Security 上下文解析开放授权主体。
 */
public interface OpenAuthSubjectResolver {

    /**
     * 支持的 realm。
     *
     * @return 域标识
     */
    String realmId();

    /**
     * 是否支持当前认证对象。
     *
     * @param authentication Spring Security 认证对象
     * @return 是否支持
     */
    boolean supports(Authentication authentication);

    /**
     * 解析授权主体。
     *
     * @param authentication Spring Security 认证对象
     * @return 授权主体
     */
    OpenAuthSubject resolve(Authentication authentication);
}
