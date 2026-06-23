package com.mtfm.deadman.plugin.pay.constant;

/**
 * 支付方式标识常量。
 */
public final class PaymentMethod {

    /** 小程序 / 公众号 JSAPI 支付 */
    public static final String JSAPI = "JSAPI";

    /** 扫码支付（Native） */
    public static final String NATIVE = "NATIVE";

    /** APP 支付 */
    public static final String APP = "APP";

    /** H5 支付 */
    public static final String H5 = "H5";

    private PaymentMethod() {
    }
}
