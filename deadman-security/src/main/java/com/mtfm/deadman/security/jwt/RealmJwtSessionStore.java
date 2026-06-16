package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.security.token.RealmJwtSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按端隔离的 Access Token 会话存储。
 */
@Slf4j
public class RealmJwtSessionStore {

    private final RealmJwtSettings settings;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<Long, String> localSessions = new ConcurrentHashMap<>();

    /**
     * @param settings              端 JWT 配置
     * @param redisTemplateProvider Redis 模板（可选）
     */
    public RealmJwtSessionStore(
            RealmJwtSettings settings, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.settings = settings;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    /**
     * 注册当前有效 Access 会话；单点登录模式下覆盖旧会话。
     *
     * @param userId 用户主键
     * @param jti    JWT 唯一标识
     */
    public void registerSession(Long userId, String jti) {
        if (settings.multiSessionEnabled()) {
            return;
        }
        replaceSession(userId, jti);
    }

    /**
     * 强制替换 Access 会话（Refresh 轮换时使用，使旧 Access Token 立即失效）。
     *
     * @param userId 用户主键
     * @param jti    新的 Access jti
     */
    public void replaceSession(Long userId, String jti) {
        if (userId == null || jti == null || jti.isBlank()) {
            return;
        }
        Duration ttl = Duration.ofMillis(settings.accessExpirationMs());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(sessionKey(userId), jti, ttl);
            return;
        }
        localSessions.put(userId, jti);
        log.debug("Redis 不可用，realm={} 使用内存 Access 会话存储 userId={}", settings.realm(), userId);
    }

    /**
     * 判断 jti 是否为当前有效 Access 会话。
     *
     * @param userId 用户主键
     * @param jti    JWT 唯一标识
     * @return 是否有效
     */
    public boolean isSessionActive(Long userId, String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        String activeJti = getActiveAccessJti(userId);
        if (activeJti == null) {
            return settings.multiSessionEnabled();
        }
        return jti.equals(activeJti);
    }

    /**
     * 使用户该端全部 Access 会话失效。
     *
     * @param userId 用户主键
     */
    public void invalidateUserSessions(Long userId) {
        if (userId == null) {
            return;
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(sessionKey(userId));
            return;
        }
        localSessions.remove(userId);
    }

    private String getActiveAccessJti(Long userId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(sessionKey(userId));
        }
        return localSessions.get(userId);
    }

    private String sessionKey(Long userId) {
        return settings.sessionKeyPrefix() + userId;
    }
}
