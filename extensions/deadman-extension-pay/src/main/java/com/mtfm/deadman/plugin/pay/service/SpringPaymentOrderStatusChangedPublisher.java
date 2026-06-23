package com.mtfm.deadman.plugin.pay.service;

import org.springframework.context.ApplicationEventPublisher;

import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.event.PaymentOrderStatusChangedEvent;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderStatusChangedPublisher;

import lombok.RequiredArgsConstructor;

/**
 * 基于 Spring ApplicationEvent 的支付单状态变更发布器（默认实现）。
 */
@RequiredArgsConstructor
public class SpringPaymentOrderStatusChangedPublisher implements PaymentOrderStatusChangedPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(PaymentOrder order, String previousStatus, String currentStatus) {
        applicationEventPublisher
                .publishEvent(new PaymentOrderStatusChangedEvent(order, previousStatus, currentStatus));
    }
}
