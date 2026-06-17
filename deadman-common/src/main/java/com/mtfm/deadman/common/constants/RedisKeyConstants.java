package com.mtfm.deadman.common.constants;

/**
 * Redis 业务 key 前缀，格式：业务模块:功能:参数。
 */
public final class RedisKeyConstants {

    public static final String PREFIX = "deadman";

    public static final String USER_PROFILE = PREFIX + ":user:profile:";

    /** 管理端 JWT Refresh Token jti 前缀 */
    public static final String AUTH_REFRESH = PREFIX + ":auth:refresh:";

    /** 管理端微信 OAuth 待绑定临时令牌前缀 */
    public static final String ADMIN_WECHAT_BIND = PREFIX + ":wechat:admin:bind:";

    /** 用户端微信 OAuth 待绑定临时令牌前缀 */
    public static final String CLIENT_WECHAT_BIND = PREFIX + ":wechat:client:bind:";

    /** 管理端微信网页扫码 OAuth 待绑定临时令牌前缀 */
    public static final String ADMIN_WECHAT_WEB_BIND = PREFIX + ":wechat:admin:web:bind:";

    /** 用户端微信网页扫码 OAuth 待绑定临时令牌前缀 */
    public static final String CLIENT_WECHAT_WEB_BIND = PREFIX + ":wechat:client:web:bind:";

    private RedisKeyConstants() {
    }

    /**
     * 构建管理端微信待绑定临时令牌 Redis key。
     *
     * @param bindToken 临时绑定令牌
     * @return 完整 Redis key
     */
    public static String adminWechatBindKey(String bindToken) {
        return ADMIN_WECHAT_BIND + bindToken;
    }

    /**
     * 构建用户端微信待绑定临时令牌 Redis key。
     *
     * @param bindToken 临时绑定令牌
     * @return 完整 Redis key
     */
    public static String clientWechatBindKey(String bindToken) {
        return CLIENT_WECHAT_BIND + bindToken;
    }

    /**
     * 构建管理端微信网页扫码待绑定临时令牌 Redis key。
     *
     * @param bindToken 临时绑定令牌
     * @return 完整 Redis key
     */
    public static String adminWechatWebBindKey(String bindToken) {
        return ADMIN_WECHAT_WEB_BIND + bindToken;
    }

    /**
     * 构建用户端微信网页扫码待绑定临时令牌 Redis key。
     *
     * @param bindToken 临时绑定令牌
     * @return 完整 Redis key
     */
    public static String clientWechatWebBindKey(String bindToken) {
        return CLIENT_WECHAT_WEB_BIND + bindToken;
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

    /**
     * 构建管理端 Refresh Token jti Redis key。
     *
     * @param jti Refresh Token 唯一标识
     * @return 完整 Redis key
     */
    public static String authRefreshKey(String jti) {
        return AUTH_REFRESH + jti;
    }
}
