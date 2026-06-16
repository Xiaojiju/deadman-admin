package com.mtfm.deadman.component.client.constants;

/**
 * 用户端认证常量。
 */
public final class ClientAuthConstants {

    /** JWT claim：用户体系标识，固定为 CLIENT */
    public static final String JWT_REALM = "CLIENT";

    /** 用户端登录 Provider 组标识 */
    public static final String LOGIN_GROUP_ID = "client";

    /** JWT claim：对外用户编码 */
    public static final String JWT_USER_CODE = "userCode";

    /** Redis 会话 key 前缀 */
    public static final String SESSION_KEY_PREFIX = "deadman:client:auth:session:";

    /** Refresh Token jti Redis key 前缀 */
    public static final String REFRESH_KEY_PREFIX = "deadman:client:auth:refresh:";

    /** Refresh Token 用户索引 Redis key 前缀 */
    public static final String REFRESH_USER_INDEX_PREFIX = "deadman:client:auth:refresh:user:";

    /** Refresh Token 无感刷新接口路径 */
    public static final String REFRESH_TOKEN_PATH = "/client/api/auth/refresh";

    /** Refresh Token HttpOnly Cookie 名称 */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "deadman_client_refresh_token";

    private ClientAuthConstants() {
    }
}
