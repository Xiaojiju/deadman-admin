package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * C 端寄件价格查询结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 * @param price   预估价格（字符串，依渠道格式）
 * @param rawData 原始返回 JSON
 */
public record LogisticsConsumerShipPriceResult(boolean success, String message, String price, String rawData) {
}
