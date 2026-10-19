package com.mtfm.deadman.plugin.wechat.support;

/**
 * 微信登录业务码。数值与历史接口保持一致，文案键仍为 result.WECHAT_BIND_*。
 */
public final class WechatErrorCodes {

    /** 微信绑定临时令牌无效或已过期 */
    public static final int WECHAT_BIND_TOKEN_INVALID = 12032;

    private WechatErrorCodes() {}
}
