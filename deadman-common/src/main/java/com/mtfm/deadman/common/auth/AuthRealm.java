package com.mtfm.deadman.common.auth;

/**
 * 认证域（用户体系 / 端），用于 {@link RequireAuth} 标识接口所属登录主体类型。
 */
public enum AuthRealm {

    /** 管理端用户体系 */
    ADMIN,

    /** C 端用户体系 */
    CLIENT
}
