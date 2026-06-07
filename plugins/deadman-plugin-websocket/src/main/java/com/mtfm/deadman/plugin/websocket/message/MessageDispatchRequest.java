package com.mtfm.deadman.plugin.websocket.message;

import java.util.Map;

/**
 * 消息调度请求。
 *
 * @param channelCode    通道编码
 * @param messageType    消息类型
 * @param targetUserKey  目标用户标识
 * @param payload        扩展负载
 * @param maxRetry       最大重试次数，null 时使用全局配置
 */
public record MessageDispatchRequest(
        String channelCode,
        String messageType,
        String targetUserKey,
        Map<String, Object> payload,
        Integer maxRetry) {
}
