package com.mtfm.deadman.plugin.logistics.spi.track;

import java.util.List;

/**
 * 物流轨迹查询结果。
 *
 * @param providerId  实际使用的 Provider 标识
 * @param carrierCode 快递公司编码
 * @param trackingNo  快递单号
 * @param state       物流状态码（快递100 state，如 0 在途、3 签收）
 * @param signed      是否已签收（快递100 ischeck：1 已签收）
 * @param message     状态描述或错误提示
 * @param nodes       轨迹节点列表，按时间倒序（最新在前）
 */
public record LogisticsTrackQueryResult(
        String providerId,
        String carrierCode,
        String trackingNo,
        String state,
        boolean signed,
        String message,
        List<LogisticsTrackNode> nodes) {
}
