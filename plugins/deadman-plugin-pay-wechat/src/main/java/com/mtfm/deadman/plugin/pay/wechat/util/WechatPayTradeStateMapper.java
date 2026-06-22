package com.mtfm.deadman.plugin.pay.wechat.util;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;

/**
 * 微信支付 trade_state 与平台支付单状态映射。
 */
public final class WechatPayTradeStateMapper {

    private WechatPayTradeStateMapper() {
    }

    /**
     * 将微信 trade_state 映射为平台支付单状态。
     *
     * @param tradeState 微信交易状态
     * @return 平台支付单状态
     */
    public static String toPaymentStatus(String tradeState) {
        if (tradeState == null || tradeState.isBlank()) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "微信回调缺少 trade_state");
        }
        return switch (tradeState.trim().toUpperCase()) {
            case "SUCCESS" -> PaymentOrderStatus.SUCCESS;
            case "CLOSED", "REVOKED", "PAYERROR" -> PaymentOrderStatus.CLOSED;
            case "REFUND" -> PaymentOrderStatus.REFUND;
            case "NOTPAY", "USERPAYING" -> PaymentOrderStatus.NOT_PAY;
            default -> throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "未知微信 trade_state：" + tradeState);
        };
    }
}
