package com.mtfm.deadman.plugin.logistics.kuaidi100.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.Kuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsShipProvider;

import lombok.RequiredArgsConstructor;

/**
 * 快递100 寄件服务领域 Provider。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100LogisticsShipProvider implements LogisticsShipProvider {

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
    public LogisticsMerchantShipOrderResult createMerchantShipOrder(LogisticsMerchantShipOrderContext context) {
        return kuaidi100LogisticsApiGateway.createMerchantShipOrder(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipCancelResult cancelMerchantShipOrder(LogisticsMerchantShipCancelContext context) {
        return kuaidi100LogisticsApiGateway.cancelMerchantShipOrder(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipOrderResult createConsumerShipOrder(LogisticsConsumerShipOrderContext context) {
        return kuaidi100LogisticsApiGateway.createConsumerShipOrder(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipCancelResult cancelConsumerShipOrder(LogisticsConsumerShipCancelContext context) {
        return kuaidi100LogisticsApiGateway.cancelConsumerShipOrder(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipPriceResult queryConsumerShipPrice(LogisticsConsumerShipPriceContext context) {
        return kuaidi100LogisticsApiGateway.queryConsumerShipPrice(context);
    }
}
