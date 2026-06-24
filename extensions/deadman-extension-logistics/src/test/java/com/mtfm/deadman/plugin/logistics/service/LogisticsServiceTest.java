package com.mtfm.deadman.plugin.logistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.logistics.config.LogisticsPluginProperties;
import com.mtfm.deadman.plugin.logistics.manager.LogisticsCarrierCodeRegistry;
import com.mtfm.deadman.plugin.logistics.manager.LogisticsProviderRegistry;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierCodeContributor;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackNode;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierProvider;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackProvider;

/**
 * 物流查单服务单元测试。
 */
class LogisticsServiceTest {

    private LogisticsCarrierCodeRegistry carrierCodeRegistry;
    private LogisticsCarrierCodeSupport carrierCodeSupport;

    @BeforeEach
    void setUp() {
        carrierCodeRegistry = new LogisticsCarrierCodeRegistry(null);
        carrierCodeRegistry.register(new StubCarrierCodeContributor());
        carrierCodeSupport = new LogisticsCarrierCodeSupport(carrierCodeRegistry);
    }

    /**
     * 服务应委托轨迹 Provider 完成查单，并将统一编码转换为厂商编码。
     */
    @Test
    void queryTrack_delegatesToTrackProvider() {
        LogisticsPluginProperties properties = new LogisticsPluginProperties();
        properties.setDefaultProvider("stub");
        LogisticsProviderRegistry registry = new LogisticsProviderRegistry(
                List.of(new StubTrackProvider()),
                List.of(new StubCarrierProvider()),
                List.of(),
                List.of(),
                properties);
        LogisticsCacheSupport cacheSupport = new LogisticsCacheSupport(properties, null);
        LogisticsSubscribePushService pushService = new LogisticsSubscribePushService(List.of());
        LogisticsService service =
                new LogisticsService(registry, cacheSupport, pushService, carrierCodeSupport);

        LogisticsTrackQueryResult result =
                service.queryTrack(new LogisticsTrackQueryContext(LogisticsCarriers.YTO, "123", null), null);
        assertThat(result.trackingNo()).isEqualTo("123");
        assertThat(result.providerId()).isEqualTo("stub");
        assertThat(result.carrierCode()).isEqualTo(LogisticsCarriers.YTO);
    }

    /**
     * 缺少快递单号时应抛出参数错误。
     */
    @Test
    void queryTrack_missingTrackingNo_throwsBadRequest() {
        LogisticsPluginProperties properties = new LogisticsPluginProperties();
        LogisticsProviderRegistry registry =
                new LogisticsProviderRegistry(List.of(), List.of(), List.of(), List.of(), properties);
        LogisticsCacheSupport cacheSupport = new LogisticsCacheSupport(properties, null);
        LogisticsSubscribePushService pushService = new LogisticsSubscribePushService(List.of());
        LogisticsService service =
                new LogisticsService(registry, cacheSupport, pushService, carrierCodeSupport);

        assertThatThrownBy(() ->
                        service.queryTrack(new LogisticsTrackQueryContext(LogisticsCarriers.YTO, " ", null), null))
                .isInstanceOf(BusinessException.class);
    }

    /**
     * 未注册的统一编码应抛出编码未知错误。
     */
    @Test
    void queryTrack_unknownCarrierCode_throwsCarrierCodeUnknown() {
        LogisticsPluginProperties properties = new LogisticsPluginProperties();
        properties.setDefaultProvider("stub");
        LogisticsProviderRegistry registry = new LogisticsProviderRegistry(
                List.of(new StubTrackProvider()),
                List.of(),
                List.of(),
                List.of(),
                properties);
        LogisticsCacheSupport cacheSupport = new LogisticsCacheSupport(properties, null);
        LogisticsSubscribePushService pushService = new LogisticsSubscribePushService(List.of());
        LogisticsService service =
                new LogisticsService(registry, cacheSupport, pushService, carrierCodeSupport);

        assertThatThrownBy(() -> service.queryTrack(new LogisticsTrackQueryContext("UNKNOWN", "123", null), null))
                .isInstanceOf(BusinessException.class);
    }

    private static final class StubCarrierCodeContributor implements LogisticsCarrierCodeContributor {

        @Override
        public String providerId() {
            return "stub";
        }

        @Override
        public Map<String, String> contribute() {
            return Map.of(LogisticsCarriers.YTO, "yuantong");
        }
    }

    private static final class StubTrackProvider implements LogisticsTrackProvider {

        @Override
        public String providerId() {
            return "stub";
        }

        @Override
        public LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context) {
            return new LogisticsTrackQueryResult(
                    providerId(),
                    context.carrierCode(),
                    context.trackingNo(),
                    "0",
                    false,
                    "在途",
                    List.of(new LogisticsTrackNode("2026-01-01 12:00:00", null, "测试节点", "0", null, null)));
        }

        @Override
        public LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context) {
            return new LogisticsSubscribeResult(true, "200", "成功");
        }

        @Override
        public LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign) {
            return null;
        }
    }

    private static final class StubCarrierProvider implements LogisticsCarrierProvider {

        @Override
        public String providerId() {
            return "stub";
        }

        @Override
        public List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo) {
            return List.of(new LogisticsCarrierDetectResult("yuantong", "圆通", null));
        }
    }
}
