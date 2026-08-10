package com.mtfm.deadman.plugin.pay.constant;

/**
 * 退款单状态常量，各支付平台统一映射到此枚举值（对齐微信退款 status）。
 */
public final class PaymentRefundStatus {

    /** 退款处理中（已受理） */
    public static final String PROCESSING = "PROCESSING";

    /** 退款成功 */
    public static final String SUCCESS = "SUCCESS";

    /** 退款关闭 */
    public static final String CLOSED = "CLOSED";

    /** 退款异常 */
    public static final String ABNORMAL = "ABNORMAL";

    private PaymentRefundStatus() {
    }
}
