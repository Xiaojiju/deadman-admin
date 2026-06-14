package com.mtfm.deadman.plugin.datascope.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启用数据权限插件 AOP
 * 切面（{@link com.mtfm.deadman.plugin.datascope.aspect.DataScopeIgnoreAspect}）。
 */
@Configuration
@EnableAspectJAutoProxy
public class DataScopeAspectConfiguration {
}
