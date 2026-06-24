package com.mtfm.deadman.plugin.logistics.spi.ship;

/**
 * C 端寄件取消结果。
 *
 * @param success 是否成功
 * @param message 描述信息
 */
public record LogisticsConsumerShipCancelResult(boolean success, String message) {
}
