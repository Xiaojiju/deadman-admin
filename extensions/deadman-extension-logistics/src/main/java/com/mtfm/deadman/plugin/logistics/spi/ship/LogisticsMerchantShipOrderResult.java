package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * 商家寄件（官方快递）下单结果。
 *
 * @param success    是否成功
 * @param message    描述信息
 * @param orderId    渠道订单 ID
 * @param trackingNo 快递单号
 * @param taskId     任务 ID
 */
public record LogisticsMerchantShipOrderResult(
        boolean success, String message, String orderId, String trackingNo, String taskId) {
}
