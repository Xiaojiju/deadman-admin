package com.mtfm.deadman.core.autoconfigure;

import com.mtfm.deadman.core.i18n.DeadmanLocaleResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 在 Spring Boot WebMvc 自动配置之前注册语言解析器。
 * <p>
 * {@code WebMvcAutoConfiguration} 会以很高的优先级声明名为 {@code localeResolver} 的 Bean，
 * 且仅在该名称尚不存在时才创建。此配置必须先执行，默认的 {@code AcceptHeaderLocaleResolver} 才会让位。
 */
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class DeadmanLocaleResolverAutoConfiguration {

    /**
     * 供 Spring MVC 在进入控制器前解析语言。
     *
     * @return 基于 Accept-Language 的语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new DeadmanLocaleResolver();
    }
}
