package com.mtfm.deadman.plugin.logistics.spi.track;

import com.mtfm.deadman.plugin.logistics.spi.LogisticsCapabilityProvider;

/**
 * 物流轨迹领域 Provider：实时查单、订阅与推送解析。
 */
public interface LogisticsTrackProvider extends LogisticsCapabilityProvider {

    /**
     * 实时查询快递轨迹。
     *
     * @param context 查单上下文
     * @return 标准化轨迹结果
     */
    LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context);

    /**
     * 订阅快递轨迹推送。
     *
     * @param context 订阅上下文
     * @return 订阅结果
     */
    LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context);

    /**
     * 解析并验签订阅推送原始载荷。
     *
     * @param rawParam 原始 param
     * @param sign     签名
     * @return 推送载荷；验签失败返回 null
     */
    LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign);
}
