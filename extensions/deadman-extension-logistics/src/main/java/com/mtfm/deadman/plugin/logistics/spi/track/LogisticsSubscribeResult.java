package com.mtfm.deadman.plugin.logistics.spi.track;

/**
 * 物流轨迹订阅结果。
 *
 * @param success    是否订阅成功
 * @param returnCode 渠道返回码
 * @param message    描述信息
 */
public record LogisticsSubscribeResult(boolean success, String returnCode, String message) {
}
