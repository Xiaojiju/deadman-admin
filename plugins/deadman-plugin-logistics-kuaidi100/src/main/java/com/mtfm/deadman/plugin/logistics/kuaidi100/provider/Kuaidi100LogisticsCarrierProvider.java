package com.mtfm.deadman.plugin.logistics.kuaidi100.provider;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierProvider;

import lombok.RequiredArgsConstructor;

/**
 * 快递100 快递公司识别领域 Provider。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100LogisticsCarrierProvider implements LogisticsCarrierProvider {

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
    public List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo) {
        return kuaidi100LogisticsApiGateway.detectCarrier(trackingNo);
    }
}
