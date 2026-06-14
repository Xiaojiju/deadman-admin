package com.mtfm.deadman.plugin.datascope.config;

import com.mtfm.deadman.plugin.datascope.filter.DataScopeContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册数据权限 Servlet Filter（在 Spring Security 链之后执行）。
 */
@Configuration
public class DataScopeSecurityConfiguration {

    /**
     * 注册数据权限上下文 Filter，匹配管理端 API。
     *
     * @param dataScopeContextFilter 数据权限 Filter
     * @return Filter 注册 Bean
     */
    @Bean
    FilterRegistrationBean<DataScopeContextFilter> dataScopeContextFilterRegistration(
            DataScopeContextFilter dataScopeContextFilter) {
        FilterRegistrationBean<DataScopeContextFilter> registration = new FilterRegistrationBean<>(
                dataScopeContextFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(0);
        registration.setName("dataScopeContextFilter");
        return registration;
    }
}
