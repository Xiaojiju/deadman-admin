package com.mtfm.deadman.plugin.wechat.miniprogram.auth;

import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramLoginBinding;
import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramPluginProperties;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 按 {@code login-bindings} 配置为每个用户体系组动态注册微信登录 Provider Bean。
 */
@Slf4j
@Component
public class WechatMiniprogramLoginProviderRegistrar
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        WechatMiniprogramPluginProperties properties = Binder.get(environment)
                .bind("deadman.plugin.wechat-miniprogram", Bindable.of(WechatMiniprogramPluginProperties.class))
                .orElseGet(WechatMiniprogramPluginProperties::new);
        if (!properties.isEnabled()) {
            return;
        }
        for (WechatMiniprogramLoginBinding binding : properties.resolveLoginBindings()) {
            String beanName = "wechatMiniprogramLoginProvider_" + binding.groupId();
            if (registry.containsBeanDefinition(beanName)) {
                log.warn("微信登录 Provider Bean 已存在，跳过注册：{}", beanName);
                continue;
            }
            AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(
                            ConfiguredWechatMiniprogramLoginProvider.class)
                    .addConstructorArgValue(binding)
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, definition);
            log.info("已注册微信登录 Provider：groupId={} beanName={}", binding.groupId(), beanName);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 无需额外处理
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
