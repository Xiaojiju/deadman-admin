package com.mtfm.deadman.plugin.pay.spi.refund;

import java.time.LocalDateTime;

/**
 * 退款单快照，供业务层查询使用。
 *
 * @param outRefundNo          平台退款单号
 * @param outTradeNo           平台支付单号
 * @param bizOrderNo           业务订单号
 * @param providerId           Provider 标识
 * @param amountRefund         本次退款金额（分）
 * @param amountTotal          原支付订单总金额（分）
 * @param status               退款状态
 * @param channelRefundId      渠道退款单号
 * @param channelTransactionId 渠道支付单号
 * @param reason               退款原因
 * @param userReceivedAccount  退款入账账户描述
 * @param createTime           创建时间
 * @param updateTime           更新时间
 */
public record RefundOrderSnapshot(
        String outRefundNo,
        String outTradeNo,
        String bizOrderNo,
        String providerId,
        Integer amountRefund,
        Integer amountTotal,
        String status,
        String channelRefundId,
        String channelTransactionId,
        String reason,
        String userReceivedAccount,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
