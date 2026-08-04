package com.mtfm.deadman.common.spi;

import com.mtfm.deadman.common.auth.AuthRealm;

/**
 * 认证域与 Security Principal 类型的匹配 SPI。
 * <p>
 * 各用户体系模块注册实现，供 {@code @RequireAuth} 切面校验登录主体类型。
 */
public interface AuthRealmPrincipalMatcher {

    /**
     * 本匹配器负责的认证域。
     *
     * @return 认证域
     */
    AuthRealm realm();

    /**
     * 判断 principal 是否属于该认证域的有效登录用户。
     *
     * @param principal SecurityContext 中的 principal
     * @return 匹配时返回 true
     */
    boolean matches(Object principal);
}
