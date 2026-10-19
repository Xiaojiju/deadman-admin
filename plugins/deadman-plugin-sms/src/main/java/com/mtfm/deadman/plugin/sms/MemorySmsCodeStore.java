package com.mtfm.deadman.plugin.sms;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内验证码存储，用于未连接 Redis 的本地或测试环境。
 */
public class MemorySmsCodeStore implements SmsCodeStore {

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    /**
     * 记录发送间隔。键已存在且未过期时拒绝。
     *
     * @param key 限流键
     * @param ttl 间隔
     * @return 是否允许发送
     */
    @Override
    public boolean markIfAbsent(String key, Duration ttl) {
        Instant now = Instant.now();
        boolean[] allowed = {false};
        entries.compute(key, (ignored, current) -> {
            if (current != null && current.expireAt.isAfter(now)) {
                allowed[0] = false;
                return current;
            }
            allowed[0] = true;
            return new Entry("1", now.plus(ttl));
        });
        return allowed[0];
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
        entries.put(key, new Entry(code, Instant.now().plus(ttl)));
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
        Entry current = entries.get(key);
        if (current == null || current.expireAt.isBefore(Instant.now()) || !current.value.equals(code)) {
            return false;
        }
        return entries.remove(key, current);
    }

    private record Entry(String value, Instant expireAt) {
    }
}
