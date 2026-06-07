package com.mtfm.deadman.security.jwt;

import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 会话存储：单点登录模式下以 Redis（或本地兜底）记录用户当前有效 jti。
 */
@Slf4j
@Component
public class JwtSessionStore {

    private static final String KEY_PREFIX = "deadman:auth:session:";

    private final DeadmanProperties properties;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<Long, String> localSessions = new ConcurrentHashMap<>();

    public JwtSessionStore(DeadmanProperties properties, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.properties = properties;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    /**
     * 注册当前有效会话；多点登录开启时跳过。
     *
     * @param userId 用户主键
     * @param jti    JWT 唯一标识
     */
    public void registerSession(Long userId, String jti) {
        if (properties.getSecurity().isMultiSessionEnabled()) {
            return;
        }
        Duration ttl = Duration.ofMillis(properties.getJwt().getExpirationMs());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(sessionKey(userId), jti, ttl);
            return;
        }
        localSessions.put(userId, jti);
        log.debug("Redis 不可用，使用内存会话存储 userId={}", userId);
    }

    /**
     * 判断 jti 是否为当前有效会话。
     *
     * @param userId 用户主键
     * @param jti    JWT 唯一标识
     * @return 是否有效
     */
    public boolean isSessionActive(Long userId, String jti) {
        if (properties.getSecurity().isMultiSessionEnabled()) {
            return true;
        }
        if (jti == null || jti.isBlank()) {
            return false;
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        String activeJti = redisTemplate != null
                ? redisTemplate.opsForValue().get(sessionKey(userId))
                : localSessions.get(userId);
        return jti.equals(activeJti);
    }

    private String sessionKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
