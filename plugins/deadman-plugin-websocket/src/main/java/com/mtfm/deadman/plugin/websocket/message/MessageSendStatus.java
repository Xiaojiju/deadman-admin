package com.mtfm.deadman.plugin.websocket.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息投递状态。
 */
@Getter
@RequiredArgsConstructor
public enum MessageSendStatus {

    PENDING(0, "待发送"),
    SENT(1, "已发送"),
    FAILED(2, "发送失败"),
    RETRYING(3, "重试中");

    private final int value;
    private final String label;

    public static MessageSendStatus fromValue(int value) {
        for (MessageSendStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知消息状态: " + value);
    }
}
