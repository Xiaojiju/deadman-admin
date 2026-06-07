package com.mtfm.deadman.plugin.websocket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话注册表，按通道 + userKey 隔离在线连接。
 */
@Component
public class WebSocketSessionRegistry {

    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    /**
     * 注册会话。
     *
     * @param channelCode 通道编码
     * @param userKey     用户标识
     * @param session     WebSocket 会话
     */
    public void register(String channelCode, String userKey, WebSocketSession session) {
        sessions.computeIfAbsent(channelCode, key -> new ConcurrentHashMap<>()).put(userKey, session);
    }

    /**
     * 移除会话。
     *
     * @param channelCode 通道编码
     * @param userKey     用户标识
     */
    public void remove(String channelCode, String userKey) {
        Map<String, WebSocketSession> channelSessions = sessions.get(channelCode);
        if (channelSessions != null) {
            channelSessions.remove(userKey);
        }
    }

    /**
     * 获取在线会话。
     *
     * @param channelCode 通道编码
     * @param userKey     用户标识
     * @return 会话
     */
    public Optional<WebSocketSession> getSession(String channelCode, String userKey) {
        Map<String, WebSocketSession> channelSessions = sessions.get(channelCode);
        if (channelSessions == null) {
            return Optional.empty();
        }
        WebSocketSession session = channelSessions.get(userKey);
        if (session == null || !session.isOpen()) {
            channelSessions.remove(userKey);
            return Optional.empty();
        }
        return Optional.of(session);
    }
}
