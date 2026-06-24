package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * 商家寄件（官方快递）价格查询结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 * @param price   预估价格
 * @param rawData 原始响应 JSON
 */
public record LogisticsMerchantShipPriceResult(boolean success, String message, String price, String rawData) {
}
