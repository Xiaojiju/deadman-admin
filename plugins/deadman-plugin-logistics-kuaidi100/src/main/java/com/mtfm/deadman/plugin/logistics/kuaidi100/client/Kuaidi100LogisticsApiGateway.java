package com.mtfm.deadman.plugin.logistics.kuaidi100.client;

import java.util.List;

import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
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
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;

/**
 * 快递100 API 网关抽象，封装查单、订阅、面单与寄件等渠道调用。
 */
public interface Kuaidi100LogisticsApiGateway {

    /**
     * 实时查询快递轨迹。
     *
     * @param context 查单上下文
     * @return 轨迹查询结果
     */
    LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context);

    /**
     * 智能识别快递公司。
     *
     * @param trackingNo 快递单号
     * @return 可能的快递公司列表
     */
    List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo);

    /**
     * 订阅快递轨迹推送。
     *
     * @param context 订阅上下文
     * @return 订阅结果
     */
    LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context);

    /**
     * 解析并验签订阅推送原始载荷。
     *
     * @param rawParam 原始 param
     * @param sign     签名
     * @return 推送载荷；验签失败返回 null
     */
    LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign);

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
