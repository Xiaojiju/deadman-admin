package com.mtfm.deadman.plugin.websocket.dispatch;

import com.mtfm.deadman.plugin.websocket.entity.WsMessageRecord;
import com.mtfm.deadman.plugin.websocket.message.MessageSendStatus;
import com.mtfm.deadman.plugin.websocket.message.WsMessage;
import lombok.Builder;

/**
 * 消息投递结果上下文，供拦截器在发送成功或失败后处理。
 */
@Builder
public record MessageDeliveryContext(
        WsMessageRecord record,
        WsMessage message,
        MessageSendStatus finalStatus,
        boolean success,
        String errorMessage) {
}
