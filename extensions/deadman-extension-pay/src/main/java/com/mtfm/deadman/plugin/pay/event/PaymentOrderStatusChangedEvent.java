package com.mtfm.deadman.plugin.pay.event;

import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;

/**
 * 支付单状态变更事件，供业务层监听处理（如更新业务订单状态）。
 */
public record PaymentOrderStatusChangedEvent(PaymentOrder order, String previousStatus, String currentStatus) {
}
