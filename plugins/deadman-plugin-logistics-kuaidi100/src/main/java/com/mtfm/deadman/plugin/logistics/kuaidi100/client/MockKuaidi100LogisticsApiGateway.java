package com.mtfm.deadman.plugin.logistics.kuaidi100.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.kuaidi100.util.Kuaidi100TrackMapper;
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
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackNode;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * Mock 快递100 网关，用于测试环境无授权信息时验收物流链路。
 */
@Slf4j
public class MockKuaidi100LogisticsApiGateway implements Kuaidi100LogisticsApiGateway {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    /**
     * {@inheritDoc}
     * <p>
     * Mock 模式：单号包含 {@code SIGNED} 时返回已签收轨迹，否则返回在途轨迹。
     */
    @Override
    public LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context) {
        boolean signed = context.trackingNo() != null && context.trackingNo().toUpperCase().contains("SIGNED");
        String state = signed ? "3" : "0";
        List<LogisticsTrackNode> nodes = buildMockNodes(signed);
        log.info(
                "Mock 快递100查单：carrier={}, trackingNo={}, signed={}",
                context.carrierCode(),
                context.trackingNo(),
                signed);
        return new LogisticsTrackQueryResult(
                Kuaidi100ProviderIds.KUAIDI100,
                context.carrierCode(),
                context.trackingNo(),
                state,
                signed,
                signed ? "已签收" : "在途",
                nodes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo) {
        log.info("Mock 快递100识别：trackingNo={}", trackingNo);
        return List.of(new LogisticsCarrierDetectResult("yuantong", "圆通速递", "YT"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context) {
        log.info("Mock 快递100订阅：carrier={}, trackingNo={}", context.carrierCode(), context.trackingNo());
        return new LogisticsSubscribeResult(true, "200", "Mock 订阅成功");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign) {
        if (!StringUtils.hasText(rawParam) || !StringUtils.hasText(sign)) {
            return null;
        }
        LogisticsSubscribePushPayload payload =
                Kuaidi100TrackMapper.parseSubscribePushPayload(Kuaidi100ProviderIds.KUAIDI100, rawParam);
        if (payload != null) {
            return payload;
        }
        return buildFallbackSubscribePushPayload(rawParam);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsWaybillOrderResult createWaybill(LogisticsWaybillOrderContext context) {
        log.info("Mock 快递100面单下单：carrier={}, bizOrderId={}", context.carrierCode(), context.bizOrderId());
        return new LogisticsWaybillOrderResult(
                true,
                "Mock 面单下单成功",
                "MOCK-WB-" + context.bizOrderId(),
                "mock-task-001",
                "mock-label-data",
                "mock-print-data",
                "mock-kd-order-001");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsWaybillCancelResult cancelWaybill(LogisticsWaybillCancelContext context) {
        log.info("Mock 快递100面单取消：trackingNo={}", context.trackingNo());
        return new LogisticsWaybillCancelResult(true, "Mock 面单取消成功");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipOrderResult createMerchantShipOrder(LogisticsMerchantShipOrderContext context) {
        log.info("Mock 快递100商家寄件：carrier={}, bizOrderId={}", context.carrierCode(), context.bizOrderId());
        return new LogisticsMerchantShipOrderResult(
                true,
                "Mock 商家寄件下单成功",
                "mock-merchant-order-001",
                "MOCK-MS-" + context.bizOrderId(),
                "mock-task-merchant-001");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipCancelResult cancelMerchantShipOrder(LogisticsMerchantShipCancelContext context) {
        log.info("Mock 快递100商家寄件取消：orderId={}", context.orderId());
        return new LogisticsMerchantShipCancelResult(true, "Mock 商家寄件取消成功");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipOrderResult createConsumerShipOrder(LogisticsConsumerShipOrderContext context) {
        log.info("Mock 快递100 C 端寄件：carrier={}", context.carrierCode());
        return new LogisticsConsumerShipOrderResult(true, "Mock C 端寄件下单成功", "mock-consumer-order-001", "mock-task-consumer-001");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipCancelResult cancelConsumerShipOrder(LogisticsConsumerShipCancelContext context) {
        log.info("Mock 快递100 C 端寄件取消：orderId={}", context.orderId());
        return new LogisticsConsumerShipCancelResult(true, "Mock C 端寄件取消成功");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipPriceResult queryConsumerShipPrice(LogisticsConsumerShipPriceContext context) {
        log.info("Mock 快递100 C 端寄件询价：carrier={}", context.carrierCode());
        return new LogisticsConsumerShipPriceResult(true, "Mock 询价成功", "12.50", "{\"price\":\"12.50\"}");
    }

    private List<LogisticsTrackNode> buildMockNodes(boolean signed) {
        List<LogisticsTrackNode> nodes = new ArrayList<>();
        nodes.add(new LogisticsTrackNode(
                "2026-06-22 10:00:00",
                "2026-06-22 10:00:00",
                signed ? "您的快件已签收，感谢使用" : "快件已到达【上海转运中心】",
                signed ? "3" : "0",
                "上海市",
                "上海,上海市"));
        nodes.add(new LogisticsTrackNode(
                "2026-06-21 18:30:00",
                "2026-06-21 18:30:00",
                "快件已从【杭州转运中心】发出",
                "0",
                "杭州市",
                "浙江,杭州市"));
        return Collections.unmodifiableList(nodes);
    }

    private LogisticsSubscribePushPayload buildFallbackSubscribePushPayload(String rawParam) {
        try {
            var root = JSON_MAPPER.readTree(rawParam);
            String carrierCode = root.path("com").asString("yuantong");
            String trackingNo = root.path("nu").asString("MOCK-NO");
            boolean signed = "1".equals(root.path("ischeck").asString("0"));
            return new LogisticsSubscribePushPayload(
                    Kuaidi100ProviderIds.KUAIDI100,
                    carrierCode,
                    trackingNo,
                    root.path("state").asString("0"),
                    signed,
                    signed ? "已签收" : "在途",
                    buildMockNodes(signed),
                    rawParam);
        } catch (Exception ex) {
            return new LogisticsSubscribePushPayload(
                    Kuaidi100ProviderIds.KUAIDI100,
                    "yuantong",
                    "MOCK-NO",
                    "0",
                    false,
                    "Mock 推送",
                    buildMockNodes(false),
                    rawParam);
        }
    }
}
