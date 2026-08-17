package com.mtfm.deadman.plugin.pay.constant;

/**
 * 平台资金账户类型（对齐微信基本账户 / 运营账户；支付宝等用业务规则模拟）。
 */
public final class PlatformFundAccountType {

    /** 基本账户：交易/分账资金池 */
    public static final String BASIC = "BASIC";

    /** 运营账户：营销与商家转账出资池 */
    public static final String OPERATION = "OPERATION";

    private PlatformFundAccountType() {
    }
}
