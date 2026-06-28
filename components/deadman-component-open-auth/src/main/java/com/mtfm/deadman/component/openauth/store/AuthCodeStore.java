package com.mtfm.deadman.component.openauth.store;

import java.util.Optional;

/**
 * 授权码存储抽象，支持 Redis 或内存实现。
 */
public interface AuthCodeStore {

    /**
     * 保存授权码。
     *
     * @param code       授权码
     * @param payload    载荷
     * @param ttlSeconds 有效期（秒）
     */
    void save(String code, AuthCodePayload payload, long ttlSeconds);

    /**
     * 消费授权码（读取并删除）。
     *
     * @param code 授权码
     * @return 载荷，不存在时为空
     */
    Optional<AuthCodePayload> consume(String code);
}
