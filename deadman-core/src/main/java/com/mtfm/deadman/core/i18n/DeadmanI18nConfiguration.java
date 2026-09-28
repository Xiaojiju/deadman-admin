package com.mtfm.deadman.core.i18n;

import com.mtfm.deadman.common.spi.MessageBasenameContributor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;

/**
 * 注册多模块文案源与请求语言解析。各模块只需投放 {@code i18n/<模块名>/messages*.properties}。
 */
@Configuration
public class DeadmanI18nConfiguration {

    /**
     * 聚合各模块资源包。同名键以先注册的模块为准。
     *
     * @param contributors 约定目录之外的资源包贡献者
     * @return 应用级文案源
     */
    @Bean
    public MessageSource messageSource(List<MessageBasenameContributor> contributors) {
        return DeadmanMessageSources.create(DeadmanMessageSources.collectBasenames(contributors));
    }

    /**
     * 供 Spring MVC 在进入控制器前解析语言。
     *
     * @return 基于 Accept-Language 的语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new DeadmanLocaleResolver();
    }

    /**
     * 尽早绑定请求语言，覆盖安全过滤器中产生的响应。
     *
     * @return 语言过滤器注册
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<DeadmanLocaleFilter> deadmanLocaleFilter() {
        FilterRegistrationBean<DeadmanLocaleFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DeadmanLocaleFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
