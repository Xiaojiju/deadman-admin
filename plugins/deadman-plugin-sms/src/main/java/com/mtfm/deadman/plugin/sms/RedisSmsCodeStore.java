package com.mtfm.deadman.plugin.sms;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 的验证码存储，多实例共用。
 */
public class RedisSmsCodeStore implements SmsCodeStore {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用项目统一的 RedisTemplate。
     *
     * @param redisTemplate Redis 操作模板
     */
    public RedisSmsCodeStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 用 SETNX 控制发送间隔。
     *
     * @param key 限流键
     * @param ttl 间隔
     * @return 是否允许发送
     */
    @Override
    public boolean markIfAbsent(String key, Duration ttl) {
        Boolean created = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(created);
    }

    /**
     * 保存验证码。
     *
     * @param key 验证码键
     * @param code 验证码
     * @param ttl 有效期
     */
    @Override
    public void save(String key, String code, Duration ttl) {
        redisTemplate.opsForValue().set(key, code, ttl);
    }

    /**
     * 匹配后删除验证码。
     *
     * @param key 验证码键
     * @param code 用户提交的验证码
     * @return 是否匹配
     */
    @Override
    public boolean consumeIfMatches(String key, String code) {
        Object current = redisTemplate.opsForValue().get(key);
        if (current == null || !code.equals(String.valueOf(current))) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }
}
