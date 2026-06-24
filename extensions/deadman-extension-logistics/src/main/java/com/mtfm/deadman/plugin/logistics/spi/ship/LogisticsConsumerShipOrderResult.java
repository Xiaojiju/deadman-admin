package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * C 端寄件下单结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 * @param orderId 渠道订单 ID
 * @param taskId  任务 ID
 */
public record LogisticsConsumerShipOrderResult(boolean success, String message, String orderId, String taskId) {
}
