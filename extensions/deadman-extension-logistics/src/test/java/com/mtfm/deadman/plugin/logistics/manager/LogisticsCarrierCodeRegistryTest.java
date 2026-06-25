package com.mtfm.deadman.plugin.logistics.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierCodeContributor;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers;

/**
 * 快递公司编码注册表单元测试。
 */
class LogisticsCarrierCodeRegistryTest {

    private LogisticsCarrierCodeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new LogisticsCarrierCodeRegistry(null);
        registry.register(new TestContributor());
    }

    /**
     * 统一编码应正确映射为厂商编码。
     */
    @Test
    void toProviderCode_mapsUnifiedToProvider() {
        assertThat(registry.toProviderCode("test", LogisticsCarriers.YTO)).isEqualTo("yuantong");
        assertThat(registry.toProviderCode("test", "yto")).isEqualTo("yuantong");
    }

    /**
     * 厂商编码应正确映射为统一编码。
     */
    @Test
    void toUnifiedCode_mapsProviderToUnified() {
        assertThat(registry.toUnifiedCode("test", "yuantong")).isEqualTo(LogisticsCarriers.YTO);
    }

    /**
     * 未注册统一编码应抛出业务异常。
     */
    @Test
    void toProviderCode_unknownUnified_throws() {
        assertThatThrownBy(() -> registry.toProviderCode("test", "UNKNOWN"))
                .isInstanceOf(BusinessException.class);
    }

    private static final class TestContributor implements LogisticsCarrierCodeContributor {

        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public Map<String, String> contribute() {
            return Map.of(LogisticsCarriers.YTO, "yuantong", LogisticsCarriers.SF, "shunfeng");
        }
    }
}
