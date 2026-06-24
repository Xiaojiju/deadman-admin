package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * 商家寄件（官方快递）取消结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 */
public record LogisticsMerchantShipCancelResult(boolean success, String message) {
}
