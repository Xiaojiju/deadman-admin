package com.mtfm.deadman.plugin.pay.constant;

/**
 * 支付平台标识常量。
 */
public final class PaymentPlatform {

    /** 微信支付 */
    public static final String WECHAT = "WECHAT";

    /** 支付宝 */
    public static final String ALIPAY = "ALIPAY";

    /** Mock 模拟支付（仅用于本地/测试） */
    public static final String MOCK = "MOCK";

    private PaymentPlatform() {
    }
}
