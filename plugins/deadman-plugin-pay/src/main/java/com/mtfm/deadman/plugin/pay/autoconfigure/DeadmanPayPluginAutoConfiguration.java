package com.mtfm.deadman.plugin.pay.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.service.DefaultPaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.service.SpringPaymentOrderStatusChangedPublisher;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderStatusChangedPublisher;
import com.mtfm.deadman.plugin.pay.spi.PaymentOutTradeNoSupplier;

/**
 * 支付插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(PayPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.plugin.pay.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.pay")
public class DeadmanPayPluginAutoConfiguration {

    /**
     * 注册默认平台支付单号生成器，宿主可声明同名类型 Bean 覆盖。
     *
     * @return 单号生成 SPI 默认实现
     */
    @Bean
    @ConditionalOnMissingBean
    public PaymentOutTradeNoSupplier paymentOutTradeNoSupplier() {
        return new DefaultPaymentOutTradeNoSupplier();
    }

    /**
     * 注册默认 Spring 事件发布器，宿主可声明 {@link PaymentOrderStatusChangedPublisher} Bean 覆盖（如
     * MQ）。
     *
     * @param publisher Spring 事件发布实现
     * @return 状态变更发布 SPI
     */
    @Bean
    @ConditionalOnMissingBean(PaymentOrderStatusChangedPublisher.class)
    public PaymentOrderStatusChangedPublisher paymentOrderStatusChangedPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        return new SpringPaymentOrderStatusChangedPublisher(applicationEventPublisher);
    }
}
