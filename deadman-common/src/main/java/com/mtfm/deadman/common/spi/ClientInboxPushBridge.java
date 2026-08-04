package com.mtfm.deadman.common.spi;

import java.util.Map;

/**
 * 用户端站内信 WebSocket 推送桥接（由 app 层注入 mobile 通道实现）。
 */
public interface ClientInboxPushBridge {

    /**
     * 向指定用户推送站内信实时通知。
     *
     * @param clientUserId 用户端用户 ID
     * @param payload 推送载荷（含 notificationId、title、content 等）
     */
    void push(Long clientUserId, Map<String, Object> payload);
}
