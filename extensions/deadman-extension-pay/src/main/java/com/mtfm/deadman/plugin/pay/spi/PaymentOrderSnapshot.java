package com.mtfm.deadman.plugin.pay.spi;

import java.time.LocalDateTime;

/**
 * 支付单快照，供业务层查单使用。
 *
 * @param outTradeNo             平台支付单号
 * @param bizOrderNo             业务订单号
 * @param providerId             支付 Provider 标识
 * @param payPlatform            支付平台
 * @param payMethod              支付方式
 * @param description            商品描述
 * @param amountTotal            订单金额（分）
 * @param status                 支付状态
 * @param channelPrepayId        渠道预支付 ID
 * @param channelTransactionId   渠道支付单号
 * @param createTime             创建时间
 * @param updateTime             更新时间
 */
public record PaymentOrderSnapshot(
        String outTradeNo,
        String bizOrderNo,
        String providerId,
        String payPlatform,
        String payMethod,
        String description,
        Integer amountTotal,
        String status,
        String channelPrepayId,
        String channelTransactionId,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
