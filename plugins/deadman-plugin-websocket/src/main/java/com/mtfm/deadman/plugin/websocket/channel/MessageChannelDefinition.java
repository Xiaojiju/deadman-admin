package com.mtfm.deadman.plugin.websocket.channel;

/**
 * 消息通道定义。
 *
 * @param code        通道编码，如 admin、mobile
 * @param description 通道说明
 */
public record MessageChannelDefinition(String code, String description) {
}
