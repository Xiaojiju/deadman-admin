package com.mtfm.deadman.plugin.websocket.config;

import com.mtfm.deadman.plugin.websocket.channel.MessageChannelRegistry;
import com.mtfm.deadman.plugin.websocket.session.ChannelWebSocketHandler;
import com.mtfm.deadman.plugin.websocket.session.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Spring WebSocket 端点注册：按通道隔离路径。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketPluginProperties properties;
    private final ChannelWebSocketHandler channelWebSocketHandler;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final MessageChannelRegistry channelRegistry;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (var channel : channelRegistry.listAll()) {
            String path = properties.getEndpointPath() + "/" + channel.code();
            registry.addHandler(channelWebSocketHandler, path)
                    .addInterceptors(new ChannelPathHandshakeInterceptor(channel.code()), handshakeInterceptor, new HttpSessionHandshakeInterceptor())
                    .setAllowedOriginPatterns("*");
        }
    }
}
