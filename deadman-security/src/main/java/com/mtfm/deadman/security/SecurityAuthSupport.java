package com.mtfm.deadman.security;

import com.mtfm.deadman.common.util.AuthPrincipalSupport;

/**
 * 管理端控制器鉴权辅助方法。
 * <p>
 * 常规「必须登录」接口请使用 {@link com.mtfm.deadman.common.auth.RequireAuth}（{@code AuthRealm.ADMIN}），
 * 本类保留给匿名与已登录均可访问、需在方法内分支处理的场景。
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
