package com.mtfm.deadman.component.client.auth.jwt;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 已登出的用户端 Access Token（按 jti）。用户端是多会话模式，Access Token 本身无状态，登出时把当前这一枚的 jti 记到它过期为止，
 * 认证过滤器遇到就当作未登录。Redis 可用时存 Redis，否则存内存。
 */
@Slf4j
@Component
public class ClientAccessTokenRevocationStore {

    private static final String KEY_PREFIX = "deadman:client:access-revoked:";

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<String, Long> localRevoked = new ConcurrentHashMap<>();

    /**
     * @param redisTemplateProvider Redis 模板（可选）
     */
    public ClientAccessTokenRevocationStore(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    /**
     * 吊销一枚 Access Token，记录到它原本的过期时间。
     *
     * @param jti Access Token 唯一标识
     * @param ttl 剩余有效期，不大于 0 时无需记录
     */
    public void revoke(String jti, Duration ttl) {
        if (jti == null || jti.isBlank() || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        long now = System.currentTimeMillis();
        localRevoked.entrySet().removeIf(entry -> entry.getValue() <= now);
        localRevoked.put(jti, now + ttl.toMillis());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttl);
        } catch (RuntimeException ex) {
            log.warn("记录已登出 Access Token 失败，仅保存在本机内存: {}", ex.getMessage());
        }
    }

    /**
     * 是否已吊销。
     *
     * @param jti Access Token 唯一标识
     * @return 已吊销返回 true
     */
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Long expireAt = localRevoked.get(jti);
        if (expireAt != null) {
            if (expireAt > System.currentTimeMillis()) {
                return true;
            }
            localRevoked.remove(jti);
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
