package com.mtfm.deadman.plugin.smslogin.auth;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.smslogin.config.SmsLoginBinding;
import com.mtfm.deadman.plugin.smslogin.config.SmsLoginPluginProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 按 {@code login-bindings} 为每个用户体系组注册短信验证码登录 Provider。
 */
@Slf4j
@Component
public class SmsCodeLoginProviderRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    /**
     * 注入环境，用于在 Bean 定义阶段读取配置。
     *
     * @param environment Spring 环境
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 注册登录 Provider Bean 定义。
     *
     * @param registry Bean 定义注册表
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        SmsLoginPluginProperties properties =
            Binder.get(environment).bind("deadman.plugin.sms-login", Bindable.of(SmsLoginPluginProperties.class))
                .orElseGet(SmsLoginPluginProperties::new);
        if (!properties.isEnabled()) {
            return;
        }
        for (SmsLoginBinding binding : properties.resolveLoginBindings()) {
            String beanName = "smsCodeLoginProvider_" + binding.groupId();
            if (registry.containsBeanDefinition(beanName)) {
                log.warn("短信登录 Provider Bean 已存在，跳过注册：{}", beanName);
                continue;
            }
            AbstractBeanDefinition definition = BeanDefinitionBuilder
                .genericBeanDefinition(ConfiguredSmsCodeLoginProvider.class).addConstructorArgValue(binding)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition();
            registry.registerBeanDefinition(beanName, definition);
            log.info("已注册短信登录 Provider：groupId={} beanName={}", binding.groupId(), beanName);
        }
    }

    /**
     * 无需改写已有 Bean。
     *
     * @param beanFactory Bean 工厂
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 无需额外处理
    }

    /**
     * 早于业务侧覆盖注册执行。
     *
     * @return 最高优先级
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
