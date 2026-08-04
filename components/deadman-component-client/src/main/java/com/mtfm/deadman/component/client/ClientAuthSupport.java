package com.mtfm.deadman.component.client;

import com.mtfm.deadman.common.util.AuthPrincipalSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;

/**
 * 用户端认证上下文工具。
 * <p>
 * 常规「必须登录」接口请使用 {@link com.mtfm.deadman.common.auth.RequireAuth}（{@code AuthRealm.CLIENT}），
 * 本类保留给匿名与已登录均可访问、需在方法内分支处理的场景。
 */
public final class ClientAuthSupport {

    private ClientAuthSupport() {}

    /**
     * 要求已登录用户。
     *
     * @param user 当前用户，可为 null
     * @return 非空用户
     */
    public static ClientLoginUser requireLogin(ClientLoginUser user) {
        return AuthPrincipalSupport.requireAuthenticated(user);
    }
}
