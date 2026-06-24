package com.mtfm.deadman.plugin.logistics.kuaidi100.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillProvider;

import lombok.RequiredArgsConstructor;

/**
 * 快递100 电子面单领域 Provider。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100LogisticsWaybillProvider implements LogisticsWaybillProvider {

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
    public LogisticsWaybillOrderResult createWaybill(LogisticsWaybillOrderContext context) {
        return kuaidi100LogisticsApiGateway.createWaybill(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsWaybillCancelResult cancelWaybill(LogisticsWaybillCancelContext context) {
        return kuaidi100LogisticsApiGateway.cancelWaybill(context);
    }
}
