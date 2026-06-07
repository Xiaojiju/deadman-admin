package com.mtfm.deadman.plugin.websocket.spi;

import java.util.Map;

/**
 * WebSocket 握手后绑定的用户身份。
 *
 * @param channelCode 消息通道编码
 * @param userKey     通道内用户唯一标识
 * @param attributes  扩展属性
 */
public record WebSocketPrincipal(String channelCode, String userKey, Map<String, Object> attributes) {
}
