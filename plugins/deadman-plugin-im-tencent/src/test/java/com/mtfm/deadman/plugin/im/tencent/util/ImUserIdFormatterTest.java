package com.mtfm.deadman.plugin.im.tencent.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

/**
 * {@link ImUserIdFormatter} 单元测试。
 */
class ImUserIdFormatterTest {

    private ImUserIdFormatter formatter;

    @BeforeEach
    void setUp() {
        ImTencentPluginProperties properties = new ImTencentPluginProperties();
        properties.setUserIdTemplate("{realm}_{subjectId}");
        formatter = new ImUserIdFormatter(properties);
    }

    @Test
    void shouldFormatUserIdWithinLimit() {
        String imUserId = formatter.format("client", "CL20260001");
        assertThat(imUserId).isEqualTo("client_CL20260001");
        assertThat(imUserId.getBytes()).hasSizeLessThanOrEqualTo(32);
    }

    @Test
    void shouldFallbackWhenTemplateTooLong() {
        String longSubject = "A".repeat(40);
        String imUserId = formatter.format("client", longSubject);
        assertThat(imUserId).startsWith("client_");
        assertThat(imUserId.getBytes()).hasSizeLessThanOrEqualTo(32);
    }
}
