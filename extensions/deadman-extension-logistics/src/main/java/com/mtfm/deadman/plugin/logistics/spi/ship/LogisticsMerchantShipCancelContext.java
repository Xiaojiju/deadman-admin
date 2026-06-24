package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * 商家寄件（官方快递）取消入参。
 *
 * @param orderId   渠道订单 ID
 * @param taskId    任务 ID
 * @param cancelMsg 取消原因
 */
public record LogisticsMerchantShipCancelContext(String orderId, String taskId, String cancelMsg) {
}
