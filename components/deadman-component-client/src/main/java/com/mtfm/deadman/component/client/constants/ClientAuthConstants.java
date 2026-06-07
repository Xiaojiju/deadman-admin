package com.mtfm.deadman.component.client.constants;

/**
 * 用户端认证常量。
 */
public final class ClientAuthConstants {

    /** JWT claim：用户体系标识，固定为 CLIENT */
    public static final String JWT_REALM = "CLIENT";

    /** JWT claim：对外用户编码 */
    public static final String JWT_USER_CODE = "userCode";

    /** Redis 会话 key 前缀 */
    public static final String SESSION_KEY_PREFIX = "deadman:client:auth:session:";

    private ClientAuthConstants() {
    }
}
