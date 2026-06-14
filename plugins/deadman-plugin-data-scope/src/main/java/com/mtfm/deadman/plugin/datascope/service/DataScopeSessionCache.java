package com.mtfm.deadman.plugin.datascope.service;

import com.mtfm.deadman.plugin.datascope.constant.DataScopeCacheNames;
import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户数据权限运行时上下文缓存（Redis / Spring Cache）。
 */
@Service
@RequiredArgsConstructor
public class DataScopeSessionCache {

    private final DataScopeContextResolver contextResolver;

    /**
     * 从缓存读取运行时上下文，未命中时加载 DB 并写入缓存。
     *
     * @param userId 用户 ID
     * @return 运行时上下文
     */
    @Cacheable(value = DataScopeCacheNames.USER_DATA_SCOPE, key = "#userId")
    public DataScopeUserContext get(Long userId) {
        return contextResolver.resolve(userId);
    }

    /**
     * 强制刷新缓存（配置或部门变更、登录预热后调用）。
     *
     * @param userId 用户 ID
     * @return 最新运行时上下文
     */
    @CachePut(value = DataScopeCacheNames.USER_DATA_SCOPE, key = "#userId")
    public DataScopeUserContext refresh(Long userId) {
        return contextResolver.resolve(userId);
    }

    /**
     * 失效用户数据权限运行时缓存。
     *
     * @param userId 用户 ID
     */
    @CacheEvict(value = DataScopeCacheNames.USER_DATA_SCOPE, key = "#userId")
    public void evict(Long userId) {
        // 缓存注解驱动失效
    }
}
