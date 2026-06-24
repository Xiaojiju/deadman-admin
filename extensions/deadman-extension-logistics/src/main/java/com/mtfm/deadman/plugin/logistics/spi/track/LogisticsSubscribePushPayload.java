package com.mtfm.deadman.plugin.logistics.spi.track;

import java.util.List;

/**
 * 物流轨迹订阅推送载荷。
 *
 * @param providerId  Provider 标识
 * @param carrierCode 快递公司编码
 * @param trackingNo  快递单号
 * @param state       物流状态码
 * @param signed      是否已签收
 * @param message     状态描述
 * @param nodes       轨迹节点
 * @param rawParam    原始推送 JSON
 */
public record LogisticsSubscribePushPayload(
        String providerId,
        String carrierCode,
        String trackingNo,
        String state,
        boolean signed,
        String message,
        List<LogisticsTrackNode> nodes,
        String rawParam) {
}
