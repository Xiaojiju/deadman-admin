package com.mtfm.deadman.system.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户与 RBAC 模块自动配置。
 */
@AutoConfiguration
@MapperScan("com.mtfm.deadman.system.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.system")
public class DeadmanSystemAutoConfiguration {
}
