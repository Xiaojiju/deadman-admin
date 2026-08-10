package com.mtfm.deadman.plugin.pay.constant;

/**
 * 支付敏感字段加解密用途（GCM AAD purpose），须与加密/解密两侧保持一致。
 */
public final class PayCryptoPurposes {

    /** 商家转账收款人姓名 */
    public static final String TRANSFER_USER_NAME = "transfer.user_name";

    private PayCryptoPurposes() {
    }
}
