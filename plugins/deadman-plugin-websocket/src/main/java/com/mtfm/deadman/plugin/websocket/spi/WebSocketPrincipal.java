package com.mtfm.deadman.plugin.websocket.spi;

/**
 * WebSocket 握手成功后写入 Session 的主体信息。
 *
 * @param channelCode  通道编码
 * @param userKey      通道内用户标识
 * @param attributes   扩展属性
 */
public record WebSocketPrincipal(String channelCode, String userKey, WebSocketPrincipalAttributes attributes) {
}
