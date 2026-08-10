package com.mtfm.deadman.plugin.pay.constant;

import org.springframework.util.StringUtils;

/**
 * 异常退款入账方式常量（对齐微信 AbnormalReceiveType）。
 */
public final class AbnormalRefundReceiveType {

    /** 退款到用户银行卡（需提供银行卡与姓名） */
    public static final String USER_BANK_CARD = "USER_BANK_CARD";

    /** 退款至交易商户银行账户 */
    public static final String MERCHANT_BANK_CARD = "MERCHANT_BANK_CARD";

    private AbnormalRefundReceiveType() {
    }

    /**
     * 是否表示资金退回商户账户（非退给买家）。
     *
     * @param userReceivedAccount 入账账户描述
     * @return true 表示商户回收
     */
    public static boolean isMerchantRecovery(String userReceivedAccount) {
        if (!StringUtils.hasText(userReceivedAccount)) {
            return false;
        }
        String normalized = userReceivedAccount.trim().toUpperCase();
        return normalized.equals(MERCHANT_BANK_CARD) || normalized.startsWith(MERCHANT_BANK_CARD + ":");
    }
}
