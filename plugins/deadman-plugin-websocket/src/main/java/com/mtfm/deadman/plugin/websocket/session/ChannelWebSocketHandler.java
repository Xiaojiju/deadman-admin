package com.mtfm.deadman.plugin.websocket.session;

import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 通道 WebSocket 处理器：维护会话注册表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelWebSocketHandler implements WebSocketHandler {

    private final WebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketPrincipal principal = (WebSocketPrincipal) session.getAttributes().get(WebSocketHandshakeInterceptor.ATTR_PRINCIPAL);
        if (principal == null) {
            return;
        }
        sessionRegistry.register(principal.channelCode(), principal.userKey(), session);
        log.debug("WebSocket 已连接 channel={} userKey={}", principal.channelCode(), principal.userKey());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof PingMessage ping) {
            sendMessage(session, new PongMessage(ping.getPayload()));
            return;
        }
        if (message instanceof TextMessage textMessage && isPingPayload(textMessage.getPayload())) {
            sendMessage(session, new TextMessage("pong"));
        }
    }

    private static boolean isPingPayload(CharSequence payload) {
        return "ping".equalsIgnoreCase(payload.toString().trim());
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage<?> outbound) throws Exception {
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(outbound);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输错误 sessionId={}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        WebSocketPrincipal principal = (WebSocketPrincipal) session.getAttributes().get(WebSocketHandshakeInterceptor.ATTR_PRINCIPAL);
        if (principal != null) {
            sessionRegistry.remove(principal.channelCode(), principal.userKey());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
