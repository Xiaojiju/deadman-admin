package com.mtfm.deadman.security.token;

/**
 * 单端 JWT 配置（密钥、时效、Redis key 前缀、Refresh Cookie 策略）。
 *
 * @param realm                     端标识，如 ADMIN / CLIENT
 * @param secret                    HMAC 密钥
 * @param accessExpirationMs        Access Token 有效期（毫秒）
 * @param refreshExpirationMs       Refresh Token 有效期（毫秒）
 * @param multiSessionEnabled       是否允许多点登录
 * @param sessionKeyPrefix          Access 会话 Redis key 前缀
 * @param refreshKeyPrefix          Refresh jti Redis key 前缀
 * @param refreshUserIndexPrefix    Refresh 用户索引 Redis key 前缀
 * @param refreshCookieSecure       Refresh Cookie Secure 属性
 * @param refreshTokenPath          无感刷新接口路径
 * @param refreshTokenCookieName    Refresh Cookie 名称
 * @param refreshTokenCookiePath    Refresh Cookie Path
 */
public record RealmJwtSettings(
        String realm,
        String secret,
        long accessExpirationMs,
        long refreshExpirationMs,
        boolean multiSessionEnabled,
        String sessionKeyPrefix,
        String refreshKeyPrefix,
        String refreshUserIndexPrefix,
        boolean refreshCookieSecure,
        String refreshTokenPath,
        String refreshTokenCookieName,
        String refreshTokenCookiePath) {
}
