package com.mtfm.deadman.plugin.pay.alipay.constant;

/**
 * 支付宝支付 Provider 标识常量。
 */
public final class AlipayPayProviderIds {

    /** 支付宝小程序 / JSAPI 直连支付（待实现） */
    public static final String ALIPAY_JSAPI = "alipay-jsapi";

    /** 支付宝二级商户进件（待实现） */
    public static final String ALIPAY_SUB_MERCHANT = "alipay-sub-merchant";

    /** 支付宝商家转账 / 单笔转账（待实现，必须走 PayoutFacade → OPERATION） */
    public static final String ALIPAY_TRANSFER = "alipay-transfer";

    private AlipayPayProviderIds() {
    }
}
