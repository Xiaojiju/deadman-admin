package com.mtfm.deadman.plugin.pay.constant;

/**
 * 微信支付分服务订单状态（对齐微信 {@code state}）。
 */
public final class PayScoreOrderState {

    /** 商户已创建服务订单 */
    public static final String CREATED = "CREATED";

    /** 服务订单进行中 */
    public static final String DOING = "DOING";

    /** 服务订单已完成 */
    public static final String DONE = "DONE";

    /** 商户取消服务订单 */
    public static final String REVOKED = "REVOKED";

    /** 服务订单已失效 */
    public static final String EXPIRED = "EXPIRED";

    private PayScoreOrderState() {
    }
}
