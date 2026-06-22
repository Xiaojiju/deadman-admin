package com.mtfm.deadman.plugin.pay.wechat.constant;

/**
 * 微信 JSAPI 支付渠道扩展参数键。
 */
public final class WechatPayChannelParams {

    /** 付款人 openid，业务层从 OAuth 账号解析后传入 */
    public static final String OPENID = "openid";

    private WechatPayChannelParams() {
    }
}
