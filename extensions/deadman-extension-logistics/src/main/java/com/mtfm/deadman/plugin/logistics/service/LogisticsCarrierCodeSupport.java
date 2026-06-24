package com.mtfm.deadman.plugin.logistics.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.logistics.manager.LogisticsCarrierCodeRegistry;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;

import lombok.RequiredArgsConstructor;

/**
 * 快递公司编码转换支持：在 Service 层完成统一编码与厂商编码的双向映射。
 */
@Component
@RequiredArgsConstructor
public class LogisticsCarrierCodeSupport {

    private final LogisticsCarrierCodeRegistry logisticsCarrierCodeRegistry;

    /**
     * 将查单上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    查单上下文（统一编码）
     * @return 厂商编码上下文
     */
    public LogisticsTrackQueryContext toProviderTrackQueryContext(
            String providerId, LogisticsTrackQueryContext context) {
        return new LogisticsTrackQueryContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.trackingNo(),
                context.phone());
    }

    /**
     * 将查单结果中的厂商编码转换为统一编码。
     *
     * @param providerId Provider 标识
     * @param result     查单结果（厂商编码）
     * @return 统一编码结果
     */
    public LogisticsTrackQueryResult toUnifiedTrackQueryResult(
            String providerId, LogisticsTrackQueryResult result) {
        return new LogisticsTrackQueryResult(
                result.providerId(),
                mapToUnifiedCode(providerId, result.carrierCode()),
                result.trackingNo(),
                result.state(),
                result.signed(),
                result.message(),
                result.nodes());
    }

    /**
     * 将订阅上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    订阅上下文
     * @return 厂商编码上下文
     */
    public LogisticsSubscribeContext toProviderSubscribeContext(
            String providerId, LogisticsSubscribeContext context) {
        return new LogisticsSubscribeContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.trackingNo(),
                context.phone(),
                context.fromAddress(),
                context.toAddress(),
                context.callbackUrl());
    }

    /**
     * 将订阅推送载荷中的厂商编码转换为统一编码。
     *
     * @param providerId Provider 标识
     * @param payload    推送载荷
     * @return 统一编码载荷
     */
    public LogisticsSubscribePushPayload toUnifiedSubscribePushPayload(
            String providerId, LogisticsSubscribePushPayload payload) {
        return new LogisticsSubscribePushPayload(
                payload.providerId(),
                mapToUnifiedCode(providerId, payload.carrierCode()),
                payload.trackingNo(),
                payload.state(),
                payload.signed(),
                payload.message(),
                payload.nodes(),
                payload.rawParam());
    }

    /**
     * 将识别结果列表中的厂商编码转换为统一编码。
     *
     * @param providerId Provider 标识
     * @param results    识别结果
     * @return 统一编码结果列表
     */
    public List<LogisticsCarrierDetectResult> toUnifiedCarrierDetectResults(
            String providerId, List<LogisticsCarrierDetectResult> results) {
        return results.stream()
                .map(result -> new LogisticsCarrierDetectResult(
                        mapToUnifiedCode(providerId, result.carrierCode()),
                        result.carrierName(),
                        result.lengthPre()))
                .toList();
    }

    /**
     * 将面单下单上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    面单上下文
     * @return 厂商编码上下文
     */
    public LogisticsWaybillOrderContext toProviderWaybillOrderContext(
            String providerId, LogisticsWaybillOrderContext context) {
        return new LogisticsWaybillOrderContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.bizOrderId(),
                context.receiver(),
                context.sender(),
                context.cargo(),
                context.weight(),
                context.count(),
                context.remark(),
                context.payType(),
                context.expType(),
                context.tempId(),
                context.siid(),
                context.needSubscribe(),
                context.pollCallbackUrl());
    }

    /**
     * 将面单取消上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    取消上下文
     * @return 厂商编码上下文
     */
    public LogisticsWaybillCancelContext toProviderWaybillCancelContext(
            String providerId, LogisticsWaybillCancelContext context) {
        return new LogisticsWaybillCancelContext(
                mapToProviderCodeIfPresent(providerId, context.carrierCode()),
                context.trackingNo(),
                context.taskId(),
                context.orderId());
    }

    /**
     * 将商家寄件下单上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    商家寄件上下文
     * @return 厂商编码上下文
     */
    public LogisticsMerchantShipOrderContext toProviderMerchantShipOrderContext(
            String providerId, LogisticsMerchantShipOrderContext context) {
        return new LogisticsMerchantShipOrderContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.bizOrderId(),
                context.receiver(),
                context.sender(),
                context.cargo(),
                context.weight(),
                context.remark(),
                context.serviceType(),
                context.dayType(),
                context.pickupStartTime(),
                context.pickupEndTime(),
                context.callbackUrl(),
                context.pollCallbackUrl());
    }

    /**
     * 将商家寄件取消上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    取消上下文
     * @return 厂商编码上下文
     */
    public LogisticsMerchantShipCancelContext toProviderMerchantShipCancelContext(
            String providerId, LogisticsMerchantShipCancelContext context) {
        return new LogisticsMerchantShipCancelContext(
                context.orderId(),
                context.taskId(),
                mapToProviderCodeIfPresent(providerId, context.carrierCode()),
                context.trackingNo(),
                context.cancelMsg());
    }

    /**
     * 将 C 端寄件下单上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    C 端寄件上下文
     * @return 厂商编码上下文
     */
    public LogisticsConsumerShipOrderContext toProviderConsumerShipOrderContext(
            String providerId, LogisticsConsumerShipOrderContext context) {
        return new LogisticsConsumerShipOrderContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.receiver(),
                context.sender(),
                context.cargo(),
                context.weight(),
                context.remark(),
                context.dayType(),
                context.pickupStartTime(),
                context.pickupEndTime(),
                context.callbackUrl(),
                context.payment(),
                context.expType());
    }

    /**
     * 将 C 端寄件取消上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    取消上下文
     * @return 厂商编码上下文
     */
    public LogisticsConsumerShipCancelContext toProviderConsumerShipCancelContext(
            String providerId, LogisticsConsumerShipCancelContext context) {
        return context;
    }

    /**
     * 将 C 端寄件询价上下文中的统一编码转换为厂商编码。
     *
     * @param providerId Provider 标识
     * @param context    询价上下文
     * @return 厂商编码上下文
     */
    public LogisticsConsumerShipPriceContext toProviderConsumerShipPriceContext(
            String providerId, LogisticsConsumerShipPriceContext context) {
        return new LogisticsConsumerShipPriceContext(
                mapToProviderCode(providerId, context.carrierCode()),
                context.sender(),
                context.receiver(),
                context.weight(),
                context.serviceType());
    }

    private String mapToProviderCode(String providerId, String unifiedCode) {
        return logisticsCarrierCodeRegistry.toProviderCode(providerId, unifiedCode);
    }

    private String mapToProviderCodeIfPresent(String providerId, String unifiedCode) {
        if (!StringUtils.hasText(unifiedCode)) {
            return unifiedCode;
        }
        return logisticsCarrierCodeRegistry.toProviderCode(providerId, unifiedCode);
    }

    private String mapToUnifiedCode(String providerId, String providerCode) {
        return logisticsCarrierCodeRegistry.toUnifiedCode(providerId, providerCode);
    }
}
