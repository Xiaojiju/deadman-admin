package com.mtfm.deadman.plugin.file.biztype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.file.config.FilePluginProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 文件业务分类注册表测试。
 */
class FileBizTypeRegistryTest {

    private FileBizTypeRegistry registry;

    @BeforeEach
    void setUp() {
        FilePluginProperties properties = new FilePluginProperties();
        properties.setBizTypeStrict(true);
        @SuppressWarnings("unchecked")
        ObjectProvider<FileBizTypeContributor> contributors = mock(ObjectProvider.class);
        registry = new FileBizTypeRegistry(contributors, properties);
    }

    @Test
    void shouldRegisterAndRequireBizType() {
        registry.register("rent");
        registry.register("spare-part");

        registry.requireRegistered("rent");
        assertThat(registry.listRegistered()).containsExactly("rent", "spare-part");
    }

    @Test
    void shouldRejectUnregisteredBizTypeWhenStrict() {
        registry.register("rent");

        assertThatThrownBy(() -> registry.requireRegistered("avatar"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("业务分类未注册");
    }

    @Test
    void shouldSkipRegistryCheckWhenNotStrict() {
        FilePluginProperties properties = new FilePluginProperties();
        properties.setBizTypeStrict(false);
        @SuppressWarnings("unchecked")
        ObjectProvider<FileBizTypeContributor> contributors = mock(ObjectProvider.class);
        FileBizTypeRegistry looseRegistry = new FileBizTypeRegistry(contributors, properties);

        looseRegistry.requireRegistered("rent");
    }

    @Test
    void shouldRejectInvalidBizTypeFormat() {
        assertThatThrownBy(() -> registry.register(""))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> registry.register("1invalid"))
                .isInstanceOf(BusinessException.class);
    }
}
