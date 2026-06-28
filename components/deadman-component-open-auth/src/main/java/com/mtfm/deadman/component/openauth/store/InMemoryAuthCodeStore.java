package com.mtfm.deadman.component.openauth.store;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存授权码存储，供集成测试或无 Redis 环境使用。
 */
@Component
@ConditionalOnMissingBean(StringRedisTemplate.class)
public class InMemoryAuthCodeStore implements AuthCodeStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /**
     * 保存授权码到内存。
     *
     * @param code       授权码
     * @param payload    载荷
     * @param ttlSeconds 有效期（秒）
     */
    @Override
    public void save(String code, AuthCodePayload payload, long ttlSeconds) {
        store.put(code, new Entry(payload, Instant.now().plusSeconds(ttlSeconds)));
    }

    /**
     * 读取并删除授权码。
     *
     * @param code 授权码
     * @return 载荷
     */
    @Override
    public Optional<AuthCodePayload> consume(String code) {
        Entry entry = store.remove(code);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entry.payload());
    }

    /**
     * 内存条目。
     *
     * @param payload   载荷
     * @param expiresAt 过期时间
     */
    private record Entry(AuthCodePayload payload, Instant expiresAt) {
    }
}
