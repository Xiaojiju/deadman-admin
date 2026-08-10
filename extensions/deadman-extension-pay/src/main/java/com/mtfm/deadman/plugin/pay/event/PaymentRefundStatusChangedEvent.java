package com.mtfm.deadman.plugin.pay.event;

import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;

/**
 * 退款单状态变更事件，供业务层监听处理（如更新业务订单状态）。
 *
 * @param refundOrder    退款单
 * @param previousStatus 变更前状态
 * @param currentStatus  变更后状态
 */
public record PaymentRefundStatusChangedEvent(
        PaymentRefundOrder refundOrder, String previousStatus, String currentStatus) {
}
