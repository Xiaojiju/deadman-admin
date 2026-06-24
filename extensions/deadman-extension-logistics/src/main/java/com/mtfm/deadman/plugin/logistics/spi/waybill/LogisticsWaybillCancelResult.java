package com.mtfm.deadman.plugin.logistics.spi.waybill;

/**
 * 电子面单取消结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 */
public record LogisticsWaybillCancelResult(boolean success, String message) {
}
