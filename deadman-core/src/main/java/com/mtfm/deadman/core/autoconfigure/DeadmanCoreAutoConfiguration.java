package com.mtfm.deadman.core.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 核心基础设施自动配置（Redis、Jackson、MyBatis、密码编码器等）。
 * <p>
 * 可在 app 模块中声明同名 {@code @Configuration} 并标注 {@code @Primary} 覆盖默认 Bean。
 * 语言解析器由 {@link DeadmanLocaleResolverAutoConfiguration} 单独提前注册，避免被这里的组件扫描再次装入。
 */
@AutoConfiguration
@ComponentScan(
        basePackages = "com.mtfm.deadman.core",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DeadmanLocaleResolverAutoConfiguration.class))
public class DeadmanCoreAutoConfiguration {
}
