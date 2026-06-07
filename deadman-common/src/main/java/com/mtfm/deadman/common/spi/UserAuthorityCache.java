package com.mtfm.deadman.common.spi;

/**
 * 用户权限缓存失效 SPI，由 security 模块实现，供 system 在角色变更后调用。
 */
public interface UserAuthorityCache {

    void evictUserAuthorities(Long userId);

    void evictAllUserAuthorities();
}
