package com.mtfm.deadman.core.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 注册请求日志过滤器。默认开启，本地不需要时设置 {@code deadman.web.log-requests=false}。
 */
@Configuration
public class RequestLoggingConfiguration {

    /**
     * 尽早包装请求，保证控制器读过的正文还能打到控制台。
     *
     * @return 请求日志过滤器注册
     */
    @Bean
    @ConditionalOnProperty(prefix = "deadman.web", name = "log-requests", matchIfMissing = true)
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
