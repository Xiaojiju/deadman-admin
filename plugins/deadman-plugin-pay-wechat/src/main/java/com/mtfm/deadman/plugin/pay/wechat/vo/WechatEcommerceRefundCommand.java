package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通退款申请命令。
 *
 * @param subMchid      子商户号
 * @param outTradeNo    商户订单号（与 transactionId 二选一）
 * @param transactionId 微信订单号（与 outTradeNo 二选一）
 * @param outRefundNo   商户退款单号
 * @param reason        退款原因
 * @param refundAmount  退款金额（分）
 * @param totalAmount   原订单金额（分）
 * @param notifyUrl     退款结果回调 URL
 */
public record WechatEcommerceRefundCommand(
        String subMchid,
        String outTradeNo,
        String transactionId,
        String outRefundNo,
        String reason,
        int refundAmount,
        int totalAmount,
        String notifyUrl) {
}
