package com.mtfm.deadman.plugin.logistics.spi.ship;

import com.mtfm.deadman.plugin.logistics.spi.LogisticsCapabilityProvider;

/**
 * 寄件服务领域 Provider：商家寄件与 C 端寄件。
 */
public interface LogisticsShipProvider extends LogisticsCapabilityProvider {

    /**
     * 商家寄件下单。
     *
     * @param context 下单上下文
     * @return 下单结果
     */
    LogisticsMerchantShipOrderResult createMerchantShipOrder(LogisticsMerchantShipOrderContext context);

    /**
     * 取消商家寄件订单。
     *
     * @param context 取消上下文
     * @return 取消结果
     */
    LogisticsMerchantShipCancelResult cancelMerchantShipOrder(LogisticsMerchantShipCancelContext context);

    /**
     * C 端寄件下单。
     *
     * @param context 下单上下文
     * @return 下单结果
     */
    LogisticsConsumerShipOrderResult createConsumerShipOrder(LogisticsConsumerShipOrderContext context);

    /**
     * 取消 C 端寄件订单。
     *
     * @param context 取消上下文
     * @return 取消结果
     */
    LogisticsConsumerShipCancelResult cancelConsumerShipOrder(LogisticsConsumerShipCancelContext context);

    /**
     * 查询 C 端寄件预估价格。
     *
     * @param context 询价上下文
     * @return 询价结果
     */
    LogisticsConsumerShipPriceResult queryConsumerShipPrice(LogisticsConsumerShipPriceContext context);
}
