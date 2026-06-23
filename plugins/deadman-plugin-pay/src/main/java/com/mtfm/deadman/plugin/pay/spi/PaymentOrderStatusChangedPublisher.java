package com.mtfm.deadman.plugin.pay.spi;

import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;

/**
 * 支付单状态变更事件发布 SPI。
 * 默认使用 Spring {@code ApplicationEventPublisher}，宿主可替换为 MQ 等实现。
 */
public interface PaymentOrderStatusChangedPublisher {

    /**
     * 发布支付单状态变更通知。
     *
     * @param order          当前支付单
     * @param previousStatus 变更前状态
     * @param currentStatus  变更后状态
     */
    void publish(PaymentOrder order, String previousStatus, String currentStatus);
}
