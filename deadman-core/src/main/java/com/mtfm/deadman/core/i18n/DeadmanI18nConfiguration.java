package com.mtfm.deadman.core.i18n;

import com.mtfm.deadman.common.spi.MessageBasenameContributor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 注册多模块文案源，并尽早绑定请求语言。各模块只需投放 {@code i18n/<模块名>/messages*.properties}。
 * <p>
 * 语言解析器见 {@link com.mtfm.deadman.core.autoconfigure.DeadmanLocaleResolverAutoConfiguration}，
 * 需先于 Spring Boot 的 WebMvc 自动配置注册。
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
