package com.mtfm.deadman.plugin.logistics.spi.carrier;

import java.util.List;

import com.mtfm.deadman.plugin.logistics.spi.LogisticsCapabilityProvider;

/**
 * 快递公司识别领域 Provider。
 */
public interface LogisticsCarrierProvider extends LogisticsCapabilityProvider {

    /**
     * 智能识别快递公司。
     *
     * @param trackingNo 快递单号
     * @return 可能的快递公司列表
     */
    List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo);
}
