package com.mtfm.deadman.core.config;

import com.mtfm.deadman.common.constants.CacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境内存缓存，避免依赖 Redis。
 */
@Configuration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CacheNames.USER_PROFILE, CacheNames.USER_AUTHORITIES);
    }
}
