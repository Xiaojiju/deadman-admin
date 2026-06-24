package com.mtfm.deadman.plugin.logistics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackSubscribePushHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 物流轨迹订阅推送分发服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsSubscribePushService {

    private final List<LogisticsTrackSubscribePushHandler> pushHandlers;

    /**
     * 分发订阅推送事件到所有已注册处理器。
     *
     * @param payload 推送载荷
     */
    public void dispatch(LogisticsSubscribePushPayload payload) {
        if (payload == null) {
            return;
        }
        if (pushHandlers.isEmpty()) {
            log.info(
                    "收到物流订阅推送但无业务处理器：carrier={}, trackingNo={}, state={}",
                    payload.carrierCode(),
                    payload.trackingNo(),
                    payload.state());
            return;
        }
        for (LogisticsTrackSubscribePushHandler handler : pushHandlers) {
            handler.onTrackUpdate(payload);
        }
    }
}
