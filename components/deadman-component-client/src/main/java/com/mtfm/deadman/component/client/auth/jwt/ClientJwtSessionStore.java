package com.mtfm.deadman.component.client.auth.jwt;

import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端 JWT 会话存储，与管理端会话 key 隔离。
 */
@Slf4j
@Component
public class ClientJwtSessionStore {

    private final ClientComponentProperties properties;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<Long, String> localSessions = new ConcurrentHashMap<>();

    public ClientJwtSessionStore(
            ClientComponentProperties properties, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.properties = properties;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    /**
     * 注册当前有效会话。
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
        log.debug("Redis 不可用，用户端使用内存会话存储 userId={}", userId);
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

    /**
     * 使用户所有会话失效（禁用/注销时调用）。
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

    private String sessionKey(Long userId) {
        return ClientAuthConstants.SESSION_KEY_PREFIX + userId;
    }
}
