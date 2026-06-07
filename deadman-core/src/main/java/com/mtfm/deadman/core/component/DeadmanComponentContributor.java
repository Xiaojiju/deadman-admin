package com.mtfm.deadman.core.component;

/**
 * 组件注册贡献者 SPI。
 * <p>
 * {@code components/} 下的模块可实现此接口并注册为 Spring Bean，在应用启动时自动纳入
 * {@link DeadmanComponentRegistry}。也可在自动配置中直接声明 {@code @Bean DeadmanComponentDescriptor}。
 */
public interface DeadmanComponentContributor {

    /**
     * 提供本组件的描述信息。
     *
     * @return 组件描述符
     */
    DeadmanComponentDescriptor descriptor();
}
