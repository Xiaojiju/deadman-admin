package com.mtfm.deadman.plugin.logistics.spi.track;

/**
 * 物流轨迹订阅推送处理器 SPI，业务模块可实现此接口处理推送事件。
 */
public interface LogisticsTrackSubscribePushHandler {

    /**
     * 处理轨迹订阅推送。
     *
     * @param payload 推送载荷
     */
    void onTrackUpdate(LogisticsSubscribePushPayload payload);
}
