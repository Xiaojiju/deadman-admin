package com.mtfm.deadman.security.token;

/**
 * {@link RealmJwtSettings} 构建工厂，统一 Access 时效解析与各端 Redis key 组装。
 */
public final class RealmJwtSettingsFactory {

    private RealmJwtSettingsFactory() {
    }

    /**
     * 解析 Access Token 有效期：优先 accessExpirationMs，否则回退 expirationMs。
     *
     * @param accessExpirationMs 显式 Access 时效
     * @param expirationMs       兼容旧配置
     * @return 毫秒
     */
    public static long resolveAccessExpirationMs(long accessExpirationMs, long expirationMs) {
        return accessExpirationMs > 0 ? accessExpirationMs : expirationMs;
    }

    /**
     * 构建单端 JWT 配置。
     *
     * @param realm                    端标识
     * @param secret                   HMAC 密钥
     * @param accessExpirationMs       Access 时效
     * @param fallbackExpirationMs     Access 回退时效
     * @param refreshExpirationMs      Refresh 时效
     * @param multiSessionEnabled      是否允许多点登录
     * @param sessionKeyPrefix         Access 会话 Redis 前缀
     * @param refreshKeyPrefix         Refresh jti Redis 前缀
     * @param refreshUserIndexPrefix   Refresh 用户索引 Redis 前缀
     * @param refreshCookieSecure      Refresh Cookie Secure
     * @param refreshTokenPath         刷新接口路径
     * @param refreshTokenCookieName   Refresh Cookie 名
     * @param refreshTokenCookiePath   Refresh Cookie Path
     * @return 端 JWT 配置
     */
    public static RealmJwtSettings create(
            String realm,
            String secret,
            long accessExpirationMs,
            long fallbackExpirationMs,
            long refreshExpirationMs,
            boolean multiSessionEnabled,
            String sessionKeyPrefix,
            String refreshKeyPrefix,
            String refreshUserIndexPrefix,
            boolean refreshCookieSecure,
            String refreshTokenPath,
            String refreshTokenCookieName,
            String refreshTokenCookiePath) {
        return new RealmJwtSettings(
                realm,
                secret,
                resolveAccessExpirationMs(accessExpirationMs, fallbackExpirationMs),
                refreshExpirationMs,
                multiSessionEnabled,
                sessionKeyPrefix,
                refreshKeyPrefix,
                refreshUserIndexPrefix,
                refreshCookieSecure,
                refreshTokenPath,
                refreshTokenCookieName,
                refreshTokenCookiePath);
    }
}
