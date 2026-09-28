package com.mtfm.deadman.core.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtfm.deadman.core.autoconfigure.DeadmanLocaleResolverAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 确认自定义语言解析器先于 Spring Boot WebMvc 自动配置注册，且不会发生同名 Bean 冲突。
 */
class DeadmanLocaleResolverAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DeadmanLocaleResolverAutoConfiguration.class,
                    WebMvcAutoConfiguration.class));

    @Test
    void shouldReplaceBootLocaleResolver() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LocaleResolver.class);
            assertThat(context.getBean(LocaleResolver.class)).isInstanceOf(DeadmanLocaleResolver.class);
        });
    }
}
