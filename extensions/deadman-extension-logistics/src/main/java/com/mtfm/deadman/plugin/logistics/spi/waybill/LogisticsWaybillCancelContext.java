package com.mtfm.deadman.plugin.logistics.spi.waybill;

/**
 * 电子面单取消入参。
 *
 * @param carrierCode 快递公司编码
 * @param trackingNo  快递单号
 * @param taskId      任务 ID
 * @param orderId     订单 ID
 */
public record LogisticsWaybillCancelContext(String carrierCode, String trackingNo, String taskId, String orderId) {
}
