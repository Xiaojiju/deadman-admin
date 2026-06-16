package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.security.token.RealmJwtSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 单端 JWT 运行时支撑（签发、解析、会话与 Refresh 存储）。
 */
public record RealmJwtSupport(
        RealmJwtSettings settings,
        RealmJwtTokenProvider tokenProvider,
        RealmJwtSessionStore sessionStore,
        RealmJwtRefreshTokenStore refreshTokenStore) {

    /**
     * 根据端配置创建 JWT 运行时支撑。
     *
     * @param settings              端 JWT 配置
     * @param redisTemplateProvider Redis 模板（可选）
     * @return JWT 支撑
     */
    public static RealmJwtSupport create(
            RealmJwtSettings settings, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        RealmJwtTokenProvider tokenProvider = new RealmJwtTokenProvider(settings);
        RealmJwtSessionStore sessionStore = new RealmJwtSessionStore(settings, redisTemplateProvider);
        RealmJwtRefreshTokenStore refreshTokenStore = new RealmJwtRefreshTokenStore(settings, redisTemplateProvider);
        return new RealmJwtSupport(settings, tokenProvider, sessionStore, refreshTokenStore);
    }
}
