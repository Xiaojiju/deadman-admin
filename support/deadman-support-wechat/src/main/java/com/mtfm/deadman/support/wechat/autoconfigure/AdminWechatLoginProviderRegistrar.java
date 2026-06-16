package com.mtfm.deadman.support.wechat.autoconfigure;

import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramLoginBinding;
import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramPluginProperties;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.support.wechat.auth.AdminWechatMiniprogramLoginProvider;
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

/**
 * 为管理端组注册自定义微信登录 Provider，覆盖插件默认的 admin 实现。
 */
@Slf4j
public class AdminWechatLoginProviderRegistrar
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
        boolean adminBindingConfigured = properties.resolveLoginBindings().stream()
                .anyMatch(binding -> AdminAuthConstants.LOGIN_GROUP_ID.equals(binding.groupId()));
        if (!adminBindingConfigured) {
            log.info("微信 login-bindings 未包含 admin 组，跳过管理端微信桥接 Provider 注册");
            return;
        }

        WechatMiniprogramLoginBinding adminBinding = properties.resolveLoginBindings().stream()
                .filter(binding -> AdminAuthConstants.LOGIN_GROUP_ID.equals(binding.groupId()))
                .findFirst()
                .orElseThrow();

        String beanName = "wechatMiniprogramLoginProvider_" + AdminAuthConstants.LOGIN_GROUP_ID;
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
            log.info("已覆盖插件默认微信登录 Provider，改用管理端桥接实现：{}", beanName);
        }

        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(
                        AdminWechatMiniprogramLoginProvider.class)
                .addConstructorArgValue(adminBinding)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                .getBeanDefinition();
        registry.registerBeanDefinition(beanName, definition);
        log.info("已注册管理端微信桥接登录 Provider：groupId={} beanName={}", AdminAuthConstants.LOGIN_GROUP_ID, beanName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 无需额外处理
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
