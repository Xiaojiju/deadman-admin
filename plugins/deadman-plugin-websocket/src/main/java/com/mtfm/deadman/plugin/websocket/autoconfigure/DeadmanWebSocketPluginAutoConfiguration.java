package com.mtfm.deadman.plugin.websocket.autoconfigure;

import com.mtfm.deadman.plugin.websocket.config.WebSocketPluginProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WebSocket 消息插件自动配置。
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(WebSocketPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@MapperScan("com.mtfm.deadman.plugin.websocket.mapper")
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.websocket")
public class DeadmanWebSocketPluginAutoConfiguration {
}
