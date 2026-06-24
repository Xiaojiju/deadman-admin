package com.mtfm.deadman.plugin.logistics.kuaidi100.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackProvider;

import lombok.RequiredArgsConstructor;

/**
 * 快递100 轨迹领域 Provider。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100LogisticsTrackProvider implements LogisticsTrackProvider {

    private final Kuaidi100LogisticsApiGateway kuaidi100LogisticsApiGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return Kuaidi100ProviderIds.KUAIDI100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context) {
        return kuaidi100LogisticsApiGateway.queryTrack(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context) {
        return kuaidi100LogisticsApiGateway.subscribeTrack(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign) {
        return kuaidi100LogisticsApiGateway.parseSubscribePush(rawParam, sign);
    }
}
