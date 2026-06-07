package com.mtfm.deadman.common.constants;

/**
 * Spring Cache 缓存名称常量，与 {@link RedisKeyConstants} 前缀配合使用。
 */
public final class CacheNames {

    /** 用户资料缓存，key 为 userCode */
    public static final String USER_PROFILE = "userProfile";

    /** 用户权限码集合缓存，key 为 userId */
    public static final String USER_AUTHORITIES = "userAuthorities";

    private CacheNames() {
    }
}
