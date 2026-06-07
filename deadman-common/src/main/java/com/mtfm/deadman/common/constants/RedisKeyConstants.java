package com.mtfm.deadman.common.constants;

/**
 * Redis 业务 key 前缀，格式：业务模块:功能:参数。
 */
public final class RedisKeyConstants {

    public static final String PREFIX = "deadman";

    public static final String USER_PROFILE = PREFIX + ":user:profile:";

    private RedisKeyConstants() {
    }

    /**
     * 构建用户资料缓存 key。
     *
     * @param userCode 用户对外编码
     * @return 完整 Redis key
     */
    public static String userProfileKey(String userCode) {
        return USER_PROFILE + userCode;
    }
}
