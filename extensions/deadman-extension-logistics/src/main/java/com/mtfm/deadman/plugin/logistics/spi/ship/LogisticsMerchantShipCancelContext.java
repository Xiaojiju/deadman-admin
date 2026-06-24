package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * 商家寄件取消入参。
 *
 * @param orderId     渠道订单 ID
 * @param taskId      任务 ID
 * @param carrierCode 快递公司编码
 * @param trackingNo  快递单号
 * @param cancelMsg   取消原因
 */
public record LogisticsMerchantShipCancelContext(
        String orderId, String taskId, String carrierCode, String trackingNo, String cancelMsg) {
}
