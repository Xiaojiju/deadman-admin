package com.mtfm.deadman.plugin.logistics.spi.waybill;

/**
 * 电子面单下单结果。
 *
 * @param success      是否成功
 * @param message      描述信息
 * @param trackingNo   快递单号
 * @param taskId       任务 ID
 * @param labelData    面单数据（base64 或 HTML，依渠道返回）
 * @param printData    打印数据
 * @param kdComOrderNum 快递公司订单号
 */
public record LogisticsWaybillOrderResult(
        boolean success,
        String message,
        String trackingNo,
        String taskId,
        String labelData,
        String printData,
        String kdComOrderNum) {
}
