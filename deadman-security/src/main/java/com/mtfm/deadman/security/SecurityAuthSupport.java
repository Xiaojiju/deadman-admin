package com.mtfm.deadman.security;

import com.mtfm.deadman.common.util.AuthPrincipalSupport;

/**
 * 控制器层鉴权辅助方法。
 */
public final class SecurityAuthSupport {

    private SecurityAuthSupport() {
    }

    /**
     * 要求已登录，否则抛出未认证业务异常。
     *
     * @param loginUser 当前登录用户，可能为 null
     * @return 非空的 LoginUser
     */
    public static LoginUser requireLogin(LoginUser loginUser) {
        return AuthPrincipalSupport.requireAuthenticated(loginUser);
    }
}
