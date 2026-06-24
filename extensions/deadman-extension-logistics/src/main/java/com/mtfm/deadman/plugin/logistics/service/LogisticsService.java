package com.mtfm.deadman.plugin.logistics.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.logistics.manager.LogisticsProviderRegistry;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceResult;
import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackProvider;

import lombok.RequiredArgsConstructor;

/**
 * 物流能力编排服务，按领域 Provider 路由到对应实现。
 * <p>
 * 入参/出参中的 {@code carrierCode} 使用平台统一编码（见 {@link com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers}），
 * 调用 Provider 前自动转换为厂商编码，返回时转换回统一编码。
 */
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsProviderRegistry logisticsProviderRegistry;
    private final LogisticsCacheSupport logisticsCacheSupport;
    private final LogisticsSubscribePushService logisticsSubscribePushService;
    private final LogisticsCarrierCodeSupport logisticsCarrierCodeSupport;

    /**
     * 实时查询快递轨迹（带 Redis 短 TTL 缓存）。
     *
     * @param context    查单上下文（统一快递公司编码）
     * @param providerId Provider 标识，为空时使用默认
     * @return 轨迹查询结果（统一快递公司编码）
     */
    public LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context, String providerId) {
        validateTrackContext(context);
        LogisticsTrackProvider provider = logisticsProviderRegistry.requireTrack(providerId);
        String resolvedProviderId = provider.providerId();
        String cacheKey = logisticsCacheSupport.trackQueryCacheKey(
                resolvedProviderId,
                logisticsCacheSupport.trackQueryContextKey(
                        context.carrierCode(), context.trackingNo(), context.phone()));
        Optional<LogisticsTrackQueryResult> cached = logisticsCacheSupport.getTrackQuery(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        LogisticsTrackQueryContext providerContext =
                logisticsCarrierCodeSupport.toProviderTrackQueryContext(resolvedProviderId, context);
        LogisticsTrackQueryResult result =
                logisticsCarrierCodeSupport.toUnifiedTrackQueryResult(
                        resolvedProviderId, provider.queryTrack(providerContext));
        logisticsCacheSupport.putTrackQuery(cacheKey, result);
        return result;
    }

    /**
     * 智能识别快递公司（带 Redis 缓存）。
     *
     * @param trackingNo 快递单号
     * @param providerId Provider 标识
     * @return 可能的快递公司列表（统一编码）
     */
    public List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo, String providerId) {
        if (!StringUtils.hasText(trackingNo)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递单号不能为空");
        }
        var provider = logisticsProviderRegistry.requireCarrier(providerId);
        String resolvedProviderId = provider.providerId();
        String cacheKey =
                logisticsCacheSupport.carrierDetectCacheKey(resolvedProviderId, trackingNo.trim());
        Optional<List<LogisticsCarrierDetectResult>> cached = logisticsCacheSupport.getCarrierDetect(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<LogisticsCarrierDetectResult> results = logisticsCarrierCodeSupport.toUnifiedCarrierDetectResults(
                resolvedProviderId, provider.detectCarrier(trackingNo.trim()));
        logisticsCacheSupport.putCarrierDetect(cacheKey, results);
        return results;
    }

    /**
     * 订阅快递轨迹推送。
     *
     * @param context    订阅上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 订阅结果
     */
    public LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context, String providerId) {
        validateSubscribeContext(context);
        LogisticsTrackProvider provider = logisticsProviderRegistry.requireTrack(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsSubscribeContext providerContext =
                logisticsCarrierCodeSupport.toProviderSubscribeContext(resolvedProviderId, context);
        return provider.subscribeTrack(providerContext);
    }

    /**
     * 处理订阅推送并分发给业务处理器。
     *
     * @param providerId Provider 标识
     * @param rawParam   原始 param
     * @param sign       签名
     */
    public void handleSubscribePush(String providerId, String rawParam, String sign) {
        LogisticsTrackProvider provider = logisticsProviderRegistry.requireTrack(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsSubscribePushPayload payload = provider.parseSubscribePush(rawParam, sign);
        if (payload == null) {
            throw new BusinessException(ResultCode.LOGISTICS_SUBSCRIBE_PUSH_INVALID, "订阅推送验签失败");
        }
        LogisticsSubscribePushPayload unifiedPayload =
                logisticsCarrierCodeSupport.toUnifiedSubscribePushPayload(resolvedProviderId, payload);
        logisticsSubscribePushService.dispatch(unifiedPayload);
    }

    /**
     * 电子面单下单。
     *
     * @param context    下单上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 下单结果
     */
    public LogisticsWaybillOrderResult createWaybill(LogisticsWaybillOrderContext context, String providerId) {
        validateWaybillContext(context);
        var provider = logisticsProviderRegistry.requireWaybill(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsWaybillOrderContext providerContext =
                logisticsCarrierCodeSupport.toProviderWaybillOrderContext(resolvedProviderId, context);
        return provider.createWaybill(providerContext);
    }

    /**
     * 取消电子面单。
     *
     * @param context    取消上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 取消结果
     */
    public LogisticsWaybillCancelResult cancelWaybill(LogisticsWaybillCancelContext context, String providerId) {
        validateWaybillCancelContext(context);
        var provider = logisticsProviderRegistry.requireWaybill(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsWaybillCancelContext providerContext =
                logisticsCarrierCodeSupport.toProviderWaybillCancelContext(resolvedProviderId, context);
        return provider.cancelWaybill(providerContext);
    }

    /**
     * 商家寄件下单。
     *
     * @param context    下单上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 下单结果
     */
    public LogisticsMerchantShipOrderResult createMerchantShipOrder(
            LogisticsMerchantShipOrderContext context, String providerId) {
        validateMerchantShipContext(context);
        var provider = logisticsProviderRegistry.requireShip(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsMerchantShipOrderContext providerContext =
                logisticsCarrierCodeSupport.toProviderMerchantShipOrderContext(resolvedProviderId, context);
        return provider.createMerchantShipOrder(providerContext);
    }

    /**
     * 取消商家寄件订单。
     *
     * @param context    取消上下文
     * @param providerId Provider 标识
     * @return 取消结果
     */
    public LogisticsMerchantShipCancelResult cancelMerchantShipOrder(
            LogisticsMerchantShipCancelContext context, String providerId) {
        if (context == null || !StringUtils.hasText(context.orderId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商家寄件订单 ID 不能为空");
        }
        var provider = logisticsProviderRegistry.requireShip(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsMerchantShipCancelContext providerContext =
                logisticsCarrierCodeSupport.toProviderMerchantShipCancelContext(resolvedProviderId, context);
        return provider.cancelMerchantShipOrder(providerContext);
    }

    /**
     * C 端寄件下单。
     *
     * @param context    下单上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 下单结果
     */
    public LogisticsConsumerShipOrderResult createConsumerShipOrder(
            LogisticsConsumerShipOrderContext context, String providerId) {
        validateConsumerShipContext(context);
        var provider = logisticsProviderRegistry.requireShip(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsConsumerShipOrderContext providerContext =
                logisticsCarrierCodeSupport.toProviderConsumerShipOrderContext(resolvedProviderId, context);
        return provider.createConsumerShipOrder(providerContext);
    }

    /**
     * 取消 C 端寄件订单。
     *
     * @param context    取消上下文
     * @param providerId Provider 标识
     * @return 取消结果
     */
    public LogisticsConsumerShipCancelResult cancelConsumerShipOrder(
            LogisticsConsumerShipCancelContext context, String providerId) {
        if (context == null || !StringUtils.hasText(context.orderId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "C 端寄件订单 ID 不能为空");
        }
        return logisticsProviderRegistry.requireShip(providerId).cancelConsumerShipOrder(context);
    }

    /**
     * 查询 C 端寄件预估价格。
     *
     * @param context    询价上下文（统一快递公司编码）
     * @param providerId Provider 标识
     * @return 询价结果
     */
    public LogisticsConsumerShipPriceResult queryConsumerShipPrice(
            LogisticsConsumerShipPriceContext context, String providerId) {
        validateConsumerShipPriceContext(context);
        var provider = logisticsProviderRegistry.requireShip(providerId);
        String resolvedProviderId = provider.providerId();
        LogisticsConsumerShipPriceContext providerContext =
                logisticsCarrierCodeSupport.toProviderConsumerShipPriceContext(resolvedProviderId, context);
        return provider.queryConsumerShipPrice(providerContext);
    }

    private void validateTrackContext(LogisticsTrackQueryContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "查单参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        if (!StringUtils.hasText(context.trackingNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递单号不能为空");
        }
    }

    private void validateSubscribeContext(LogisticsSubscribeContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订阅参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        if (!StringUtils.hasText(context.trackingNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递单号不能为空");
        }
    }

    private void validateWaybillContext(LogisticsWaybillOrderContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "电子面单参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        validateContact(context.receiver(), "收件人");
        validateContact(context.sender(), "寄件人");
        if (!StringUtils.hasText(context.cargo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "货物名称不能为空");
        }
    }

    private void validateWaybillCancelContext(LogisticsWaybillCancelContext context) {
        if (context == null || !StringUtils.hasText(context.trackingNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "取消面单时快递单号不能为空");
        }
    }

    private void validateMerchantShipContext(LogisticsMerchantShipOrderContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商家寄件参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        validateContact(context.receiver(), "收件人");
        validateContact(context.sender(), "寄件人");
        if (!StringUtils.hasText(context.cargo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "货物名称不能为空");
        }
    }

    private void validateConsumerShipContext(LogisticsConsumerShipOrderContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "C 端寄件参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        validateContact(context.receiver(), "收件人");
        validateContact(context.sender(), "寄件人");
        if (!StringUtils.hasText(context.cargo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "货物名称不能为空");
        }
    }

    private void validateConsumerShipPriceContext(LogisticsConsumerShipPriceContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "询价参数不能为空");
        }
        if (!StringUtils.hasText(context.carrierCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        validateContact(context.receiver(), "收件人");
        validateContact(context.sender(), "寄件人");
    }

    private void validateContact(LogisticsContactInfo contact, String label) {
        if (contact == null
                || !StringUtils.hasText(contact.name())
                || !StringUtils.hasText(contact.mobile())
                || !StringUtils.hasText(contact.printAddress())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, label + "姓名、手机号与地址不能为空");
        }
    }
}
