package com.mtfm.deadman.plugin.wechat.miniprogram;

/**
 * 微信小程序插件常量。
 */
public final class WechatMiniprogramConstants {

    /** OAuth 提供商标识，写入各用户体系 OAuth 账号表 */
    public static final String OAUTH_PROVIDER = "wechat-miniprogram";

    /** 登录 Provider 路径段 */
    public static final String LOGIN_PATH_SEGMENT = "wechat-miniprogram";

    /** 用 getPhoneNumber code 换取手机号（不落库）的公开接口路径 */
    public static final String PHONE_RESOLVE_PATH = "/client/api/wechat-miniprogram/phone/resolve";

    private WechatMiniprogramConstants() {
    }
}
