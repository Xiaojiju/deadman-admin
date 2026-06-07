package com.mtfm.deadman.component.client.auth;

/**
 * 用户端统一认证用户负载体，供 Security 上下文与业务层使用。
 */
public interface ClientAuthenticatedUser {

    /**
     * 用户主键。
     *
     * @return 用户 ID
     */
    Long getUserId();

    /**
     * 对外用户编码。
     *
     * @return 用户编码
     */
    String getUserCode();

    /**
     * 主登录标识（如用户名），未绑定时可为 null。
     *
     * @return 登录标识
     */
    String getLoginIdentifier();

    /**
     * 用户昵称。
     *
     * @return 昵称
     */
    String getNickname();

    /**
     * 是否启用。
     *
     * @return 是否启用
     */
    boolean isEnabled();
}
