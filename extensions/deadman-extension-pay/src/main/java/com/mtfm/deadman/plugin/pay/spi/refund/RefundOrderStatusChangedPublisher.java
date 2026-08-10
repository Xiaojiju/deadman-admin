package com.mtfm.deadman.plugin.pay.spi.refund;

import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;

/**
 * 退款单状态变更事件发布 SPI。
 * 默认使用 Spring {@code ApplicationEventPublisher}，宿主可替换为 MQ 等实现。
 */
public interface RefundOrderStatusChangedPublisher {

    /**
     * 发布退款单状态变更通知。
     *
     * @param refundOrder    当前退款单
     * @param previousStatus 变更前状态
     * @param currentStatus  变更后状态
     */
    void publish(PaymentRefundOrder refundOrder, String previousStatus, String currentStatus);
}
