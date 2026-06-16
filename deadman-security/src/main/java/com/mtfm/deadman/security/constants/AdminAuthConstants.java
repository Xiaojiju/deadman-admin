package com.mtfm.deadman.security.constants;

/**
 * 管理端认证常量。
 */
public final class AdminAuthConstants {

    /** JWT claim：管理端用户体系标识 */
    public static final String JWT_REALM = "ADMIN";

    /** Access 会话 Redis key 前缀 */
    public static final String SESSION_KEY_PREFIX = "deadman:auth:session:";

    /** Refresh Token jti Redis key 前缀 */
    public static final String REFRESH_KEY_PREFIX = "deadman:auth:refresh:";

    /** Refresh Token 用户索引 Redis key 前缀 */
    public static final String REFRESH_USER_INDEX_PREFIX = "deadman:auth:refresh:user:";

    /** Refresh Token 无感刷新接口路径 */
    public static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";

    /** Refresh Token HttpOnly Cookie 名称 */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "deadman_refresh_token";

    /** 管理端登录 Provider 组标识 */
    public static final String LOGIN_GROUP_ID = "admin";

    private AdminAuthConstants() {
    }
}
