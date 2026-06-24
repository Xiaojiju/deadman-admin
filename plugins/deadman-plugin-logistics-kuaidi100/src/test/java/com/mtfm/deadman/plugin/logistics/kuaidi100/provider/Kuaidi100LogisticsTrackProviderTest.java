package com.mtfm.deadman.plugin.logistics.kuaidi100.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.plugin.logistics.kuaidi100.client.MockKuaidi100LogisticsApiGateway;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;

/**
 * 快递100 轨迹 Provider 单元测试。
 */
class Kuaidi100LogisticsTrackProviderTest {

    /**
     * Mock 网关在途单号应返回未签收轨迹。
     */
    @Test
    void queryTrack_inTransit_returnsNodes() {
        Kuaidi100LogisticsTrackProvider provider =
                new Kuaidi100LogisticsTrackProvider(new MockKuaidi100LogisticsApiGateway());
        LogisticsTrackQueryResult result = provider.queryTrack(new LogisticsTrackQueryContext("yuantong", "YT123456", null));
        assertThat(result.signed()).isFalse();
        assertThat(result.nodes()).isNotEmpty();
        assertThat(result.providerId()).isEqualTo("kuaidi100");
    }

    /**
     * Mock 网关签收单号应返回已签收状态。
     */
    @Test
    void queryTrack_signedTrackingNo_returnsSigned() {
        Kuaidi100LogisticsTrackProvider provider =
                new Kuaidi100LogisticsTrackProvider(new MockKuaidi100LogisticsApiGateway());
        LogisticsTrackQueryResult result =
                provider.queryTrack(new LogisticsTrackQueryContext("yuantong", "YT-SIGNED-001", null));
        assertThat(result.signed()).isTrue();
        assertThat(result.state()).isEqualTo("3");
    }
}
