package com.mtfm.deadman.plugin.pay.constant;

/**
 * 商户号主体类型（决定转账额度默认值与可调范围）。
 */
public final class MerchantEntityType {

    /** 非个体户 */
    public static final String NON_INDIVIDUAL = "NON_INDIVIDUAL";

    /** 个体户 */
    public static final String INDIVIDUAL = "INDIVIDUAL";

    private MerchantEntityType() {
    }
}
