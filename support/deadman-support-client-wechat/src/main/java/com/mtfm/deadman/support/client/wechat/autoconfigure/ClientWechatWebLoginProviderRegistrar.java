package com.mtfm.deadman.support.client.wechat.autoconfigure;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebLoginBinding;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebPluginProperties;
import com.mtfm.deadman.support.client.wechat.auth.ClientWechatWebLoginProvider;
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
 * 为用户端组注册微信网页扫码登录 Provider，覆盖插件默认的 client 实现。
 */
@Slf4j
public class ClientWechatWebLoginProviderRegistrar
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        WechatWebPluginProperties properties = Binder.get(environment)
                .bind("deadman.plugin.wechat-web", Bindable.of(WechatWebPluginProperties.class))
                .orElseGet(WechatWebPluginProperties::new);
        if (!properties.isEnabled()) {
            return;
        }
        boolean clientBindingConfigured = properties.resolveLoginBindings().stream()
                .anyMatch(binding -> ClientAuthConstants.LOGIN_GROUP_ID.equals(binding.groupId()));
        if (!clientBindingConfigured) {
            log.info("微信网页 login-bindings 未包含 client 组，跳过用户端网页扫码桥接 Provider 注册");
            return;
        }

        WechatWebLoginBinding clientBinding = properties.resolveLoginBindings().stream()
                .filter(binding -> ClientAuthConstants.LOGIN_GROUP_ID.equals(binding.groupId()))
                .findFirst()
                .orElseThrow();

        String beanName = "wechatWebLoginProvider_" + ClientAuthConstants.LOGIN_GROUP_ID;
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
            log.info("已覆盖插件默认微信网页登录 Provider，改用用户端桥接实现：{}", beanName);
        }

        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ClientWechatWebLoginProvider.class)
                .addConstructorArgValue(clientBinding)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                .getBeanDefinition();
        registry.registerBeanDefinition(beanName, definition);
        log.info("已注册用户端微信网页扫码桥接登录 Provider：groupId={} beanName={}", ClientAuthConstants.LOGIN_GROUP_ID, beanName);
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
