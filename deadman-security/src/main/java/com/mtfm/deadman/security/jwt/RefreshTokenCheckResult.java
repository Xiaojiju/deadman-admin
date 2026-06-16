package com.mtfm.deadman.security.jwt;

/**
 * Refresh Token 校验结果。
 */
public enum RefreshTokenCheckResult {

    /** 与当前有效 Refresh jti 一致，可继续轮换 */
    ACTIVE,

    /** 已轮换过的 Refresh jti 被再次使用，疑似令牌盗用 */
    REUSED,

    /** 无效 Refresh Token */
    INVALID
}
