package com.mtfm.deadman.security.constants;

/**
 * 管理端认证常量。
 */
public final class AdminAuthConstants {

    /** JWT claim：管理端用户体系标识 */
    public static final String JWT_REALM = "ADMIN";

    /** 管理端登录 Provider 组标识 */
    public static final String LOGIN_GROUP_ID = "admin";

    private AdminAuthConstants() {
    }
}
