package com.mtfm.deadman.support.client.file.access;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 注册敏感文件静态路径过滤器。排在 Spring Security 之前，本地存储的公开访问链放行之前先拦下。
 */
@Configuration(proxyBeanMethods = false)
public class SensitiveFileAccessConfiguration {

    /**
     * @param policy 敏感文件规则
     * @return 过滤器注册
     */
    @Bean
    public FilterRegistrationBean<SensitiveStaticFileFilter> sensitiveStaticFileFilter(SensitiveFilePolicy policy) {
        FilterRegistrationBean<SensitiveStaticFileFilter> registration =
            new FilterRegistrationBean<>(new SensitiveStaticFileFilter(policy));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("sensitiveStaticFileFilter");
        return registration;
    }
}
