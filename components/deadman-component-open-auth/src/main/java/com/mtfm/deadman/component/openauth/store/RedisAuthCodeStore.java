package com.mtfm.deadman.component.openauth.store;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.openauth.config.OpenAuthComponentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * 基于 Redis 的授权码存储实现。
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
@RequiredArgsConstructor
public class RedisAuthCodeStore implements AuthCodeStore {

    private final StringRedisTemplate redisTemplate;
    private final OpenAuthComponentProperties properties;
    private final JsonMapper jsonMapper;

    /**
     * 保存授权码到 Redis。
     *
     * @param code       授权码
     * @param payload    载荷
     * @param ttlSeconds 有效期（秒）
     */
    @Override
    public void save(String code, AuthCodePayload payload, long ttlSeconds) {
        try {
            String key = buildKey(code);
            String json = jsonMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (JacksonException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "授权码序列化失败");
        }
    }

    /**
     * 读取并删除授权码。
     *
     * @param code 授权码
     * @return 载荷
     */
    @Override
    public Optional<AuthCodePayload> consume(String code) {
        String key = buildKey(code);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        redisTemplate.delete(key);
        try {
            return Optional.of(jsonMapper.readValue(json, AuthCodePayload.class));
        } catch (JacksonException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "授权码反序列化失败");
        }
    }

    private String buildKey(String code) {
        return properties.getCode().getRedisKeyPrefix() + code;
    }
}
