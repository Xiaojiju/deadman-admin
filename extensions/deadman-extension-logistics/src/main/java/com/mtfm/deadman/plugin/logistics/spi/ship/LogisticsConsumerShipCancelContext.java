package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * C 端寄件取消入参。
 *
 * @param orderId   渠道订单 ID
 * @param taskId    任务 ID
 * @param cancelMsg 取消原因
 */
public record LogisticsConsumerShipCancelContext(String orderId, String taskId, String cancelMsg) {
}
