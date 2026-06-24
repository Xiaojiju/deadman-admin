package com.mtfm.deadman.plugin.logistics.spi.waybill;

import com.mtfm.deadman.plugin.logistics.spi.LogisticsCapabilityProvider;

/**
 * 电子面单领域 Provider。
 */
public interface LogisticsWaybillProvider extends LogisticsCapabilityProvider {

    /**
     * 电子面单下单。
     *
     * @param context 下单上下文
     * @return 下单结果
     */
    LogisticsWaybillOrderResult createWaybill(LogisticsWaybillOrderContext context);

    /**
     * 取消电子面单。
     *
     * @param context 取消上下文
     * @return 取消结果
     */
    LogisticsWaybillCancelResult cancelWaybill(LogisticsWaybillCancelContext context);
}
