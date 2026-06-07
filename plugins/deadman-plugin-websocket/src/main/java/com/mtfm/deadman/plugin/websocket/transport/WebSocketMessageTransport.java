package com.mtfm.deadman.plugin.websocket.transport;

import tools.jackson.databind.json.JsonMapper;
import com.mtfm.deadman.plugin.websocket.dispatch.MessageTransport;
import com.mtfm.deadman.plugin.websocket.message.WsMessage;
import com.mtfm.deadman.plugin.websocket.session.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 基于 Spring WebSocket 会话的消息传输实现。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageTransport implements MessageTransport {

    private final WebSocketSessionRegistry sessionRegistry;
    private final JsonMapper jsonMapper;

    @Override
    public boolean send(WsMessage message) {
        return sessionRegistry
                .getSession(message.getChannelCode(), message.getTargetUserKey())
                .map(session -> sendToSession(session, message))
                .orElse(false);
    }

    private boolean sendToSession(WebSocketSession session, WsMessage message) {
        try {
            String json = jsonMapper.writeValueAsString(message);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                    return true;
                }
            }
        } catch (Exception ex) {
            log.warn("WebSocket 写入失败 messageId={}", message.getMessageId(), ex);
        }
        return false;
    }
}
