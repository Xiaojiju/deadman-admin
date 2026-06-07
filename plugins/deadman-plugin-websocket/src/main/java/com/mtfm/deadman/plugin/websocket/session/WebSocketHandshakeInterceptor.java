package com.mtfm.deadman.plugin.websocket.session;

import com.mtfm.deadman.plugin.websocket.channel.MessageChannelRegistry;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketAuthenticator;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket 握手拦截：校验通道并按 SPI 完成鉴权。
 */
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_CHANNEL_CODE = "channelCode";
    public static final String ATTR_PRINCIPAL = "principal";

    private final MessageChannelRegistry channelRegistry;
    private final List<WebSocketAuthenticator> authenticators;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String channelCode = resolveChannelCode(request);
        if (!channelRegistry.contains(channelCode)) {
            return false;
        }
        Optional<WebSocketPrincipal> principal = authenticate(channelCode, request);
        if (principal.isEmpty()) {
            return false;
        }
        attributes.put(ATTR_CHANNEL_CODE, channelCode);
        attributes.put(ATTR_PRINCIPAL, principal.get());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }

    private String resolveChannelCode(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Object value = servletRequest.getServletRequest().getAttribute(ATTR_CHANNEL_CODE);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Optional<WebSocketPrincipal> authenticate(String channelCode, ServerHttpRequest request) {
        for (WebSocketAuthenticator authenticator : authenticators) {
            if (authenticator.supports(channelCode)) {
                return authenticator.authenticate(channelCode, request);
            }
        }
        return Optional.empty();
    }
}
