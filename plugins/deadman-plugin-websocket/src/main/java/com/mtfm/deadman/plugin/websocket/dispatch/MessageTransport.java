package com.mtfm.deadman.plugin.websocket.dispatch;

import com.mtfm.deadman.plugin.websocket.message.WsMessage;

/**
 * 消息传输 SPI，由 WebSocket 等具体实现完成投递。
 */
public interface MessageTransport {

    /**
     * 尝试将消息投递到在线会话。
     *
     * @param message 消息负载体
     * @return 是否投递成功（目标在线且写入成功）
     */
    boolean send(WsMessage message);
}
