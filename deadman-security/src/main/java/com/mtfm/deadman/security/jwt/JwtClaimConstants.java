package com.mtfm.deadman.security.jwt;

/**
 * JWT 通用 Claim 常量（与 ADMIN/CLIENT 端无关）。
 */
public final class JwtClaimConstants {

    /** Claim 字段：用户对外编码 */
    public static final String USER_CODE = "userCode";

    /** Claim 字段：端标识 realm */
    public static final String REALM = "realm";

    /** Claim 字段：令牌类型 */
    public static final String TOKEN_TYPE = "tokenType";

    /** 令牌类型：Access Token */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** 令牌类型：Refresh Token */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private JwtClaimConstants() {
    }
}
