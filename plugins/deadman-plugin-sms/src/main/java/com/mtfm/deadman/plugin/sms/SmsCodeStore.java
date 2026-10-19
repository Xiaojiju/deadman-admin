package com.mtfm.deadman.plugin.sms;

import java.time.Duration;

/**
 * 短信验证码存储。生产使用 Redis，无 Redis 时退回进程内存储。
 */
public interface SmsCodeStore {

    /**
     * 在间隔未过期时拒绝再次发送。
     *
     * @param key 限流键
     * @param ttl 间隔
     * @return true 表示本次可以发送
     */
    boolean markIfAbsent(String key, Duration ttl);

    /**
     * 保存验证码。
     *
     * @param key 验证码键
     * @param code 验证码
     * @param ttl 有效期
     */
    void save(String key, String code, Duration ttl);

    /**
     * 验证码匹配则删除并返回 true。
     *
     * @param key 验证码键
     * @param code 用户提交的验证码
     * @return 是否匹配
     */
    boolean consumeIfMatches(String key, String code);
}
