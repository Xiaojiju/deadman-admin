package com.mtfm.deadman.core.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 核心基础设施自动配置（Redis、Jackson、MyBatis、密码编码器等）。
 * <p>
 * 可在 app 模块中声明同名 {@code @Configuration} 并标注 {@code @Primary} 覆盖默认 Bean。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mtfm.deadman.core")
public class DeadmanCoreAutoConfiguration {
}
