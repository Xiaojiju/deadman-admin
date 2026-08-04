package com.mtfm.deadman.plugin.storage.cos.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;

/**
 * COS Bucket 路由解析测试。
 */
class CosBucketResolverTest {

    private CosBucketResolver resolver;

    private CosStoragePluginProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CosStoragePluginProperties();
        properties.setDefaultBucket("engineering-public");
        properties.setBucketRouting(Map.of("rent", "engineering-user-upload"));
        resolver = new CosBucketResolver(properties);
    }

    @Test
    void shouldResolveRoutedBucketByBizType() {
        assertThat(resolver.resolve("rent")).isEqualTo("engineering-user-upload");
    }

    @Test
    void shouldFallbackToDefaultBucket() {
        assertThat(resolver.resolve("unknown")).isEqualTo("engineering-public");
        assertThat(resolver.resolve(null)).isEqualTo("engineering-public");
    }

    @Test
    void shouldFailWhenDefaultBucketMissing() {
        properties.setDefaultBucket("");
        assertThatThrownBy(() -> resolver.resolve("unknown"))
                .isInstanceOf(BusinessException.class);
    }
}
