package com.mtfm.deadman.common.spi;

import java.util.List;

/**
 * 缓存 TTL 贡献者 SPI，业务/插件模块可实现并向 Redis CacheManager 注册命名缓存 TTL。
 */
public interface CacheTtlContributor {

    /**
     * 贡献命名缓存 TTL 列表。
     *
     * @return 缓存 TTL 列表
     */
    List<NamedCacheTtl> contribute();
}
