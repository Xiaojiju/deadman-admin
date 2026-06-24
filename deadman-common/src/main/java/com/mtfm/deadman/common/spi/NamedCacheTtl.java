package com.mtfm.deadman.common.spi;

import java.time.Duration;

/**
 * 命名缓存 TTL 描述，供模块向 core 注册独立 Redis 缓存过期策略。
 *
 * @param cacheName 缓存名称（与 Spring Cache value 一致）
 * @param ttl       过期时间，禁止永久缓存
 */
public record NamedCacheTtl(String cacheName, Duration ttl) {
}
