package com.mtfm.deadman.plugin.storage.oss.routing;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtfm.deadman.plugin.storage.oss.config.OssStoragePluginProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * OSS Bucket 路由解析测试。
 */
class OssBucketResolverTest {

    private OssBucketResolver resolver;

    @BeforeEach
    void setUp() {
        OssStoragePluginProperties properties = new OssStoragePluginProperties();
        properties.setDefaultBucket("engineering-public");
        properties.setBucketRouting(Map.of(
                "rent", "engineering-user-upload",
                "spare-part", "engineering-merchant"));
        resolver = new OssBucketResolver(properties);
    }

    @Test
    void shouldRouteByBizType() {
        assertThat(resolver.resolve("rent")).isEqualTo("engineering-user-upload");
        assertThat(resolver.resolve("spare-part")).isEqualTo("engineering-merchant");
    }

    @Test
    void shouldFallbackToDefaultBucketWhenBizTypeMissingOrUnknown() {
        assertThat(resolver.resolve(null)).isEqualTo("engineering-public");
        assertThat(resolver.resolve("")).isEqualTo("engineering-public");
        assertThat(resolver.resolve("unknown")).isEqualTo("engineering-public");
    }
}
