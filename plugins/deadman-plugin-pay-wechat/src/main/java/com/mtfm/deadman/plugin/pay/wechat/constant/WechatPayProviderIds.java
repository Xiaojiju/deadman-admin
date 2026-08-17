package com.mtfm.deadman.plugin.pay.wechat.constant;

/**
 * 微信支付 Provider 标识常量。
 */
public final class WechatPayProviderIds {

    /** 微信小程序 JSAPI 支付 */
    public static final String WECHAT_JSAPI = "wechat-jsapi";

    /** 微信 Native 扫码支付（预留） */
    public static final String WECHAT_NATIVE = "wechat-native";

    /** 微信收付通合单 JSAPI 支付 */
    public static final String WECHAT_ECOMMERCE_COMBINE_JSAPI = "wechat-ecommerce-combine-jsapi";

    /** 微信收付通二级商户进件 */
    public static final String WECHAT_ECOMMERCE_SUB_MERCHANT = "wechat-ecommerce-sub-merchant";

    /** 微信支付分 */
    public static final String WECHAT_PAY_SCORE = "wechat-payscore";

    private WechatPayProviderIds() {
    }
}
