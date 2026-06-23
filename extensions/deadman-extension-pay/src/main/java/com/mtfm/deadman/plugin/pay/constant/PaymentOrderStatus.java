package com.mtfm.deadman.plugin.pay.constant;

/**
 * 支付单状态常量，各支付平台统一映射到此枚举值。
 */
public final class PaymentOrderStatus {

    /** 未支付 */
    public static final String NOT_PAY = "NOT_PAY";

    /** 支付成功 */
    public static final String SUCCESS = "SUCCESS";

    /** 已关闭 */
    public static final String CLOSED = "CLOSED";

    /** 转入退款 */
    public static final String REFUND = "REFUND";

    private PaymentOrderStatus() {
    }
}
