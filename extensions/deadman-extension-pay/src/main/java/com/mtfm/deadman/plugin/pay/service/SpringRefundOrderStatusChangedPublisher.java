package com.mtfm.deadman.plugin.pay.service;

import org.springframework.context.ApplicationEventPublisher;

import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.event.PaymentRefundStatusChangedEvent;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderStatusChangedPublisher;

import lombok.RequiredArgsConstructor;

/**
 * 基于 Spring ApplicationEvent 的退款单状态变更发布器（默认实现）。
 */
@RequiredArgsConstructor
public class SpringRefundOrderStatusChangedPublisher implements RefundOrderStatusChangedPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(PaymentRefundOrder refundOrder, String previousStatus, String currentStatus) {
        applicationEventPublisher
                .publishEvent(new PaymentRefundStatusChangedEvent(refundOrder, previousStatus, currentStatus));
    }
}
