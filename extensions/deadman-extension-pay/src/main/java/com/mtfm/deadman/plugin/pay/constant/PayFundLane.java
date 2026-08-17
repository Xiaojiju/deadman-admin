package com.mtfm.deadman.plugin.pay.constant;

/**
 * 支付资金链路（业务门面分流，禁止混用统计与编排）。
 */
public final class PayFundLane {

    /** 普通直连支付（平台自营等） */
    public static final String DIRECT = "DIRECT";

    /** 收付通 / 服务商合单交易（二级商户一清） */
    public static final String ECOMMERCE = "ECOMMERCE";

    private PayFundLane() {
    }
}
