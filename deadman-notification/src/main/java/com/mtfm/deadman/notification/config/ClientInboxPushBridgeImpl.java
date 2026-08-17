package com.mtfm.deadman.notification.config;

import com.mtfm.deadman.common.spi.ClientInboxPushBridge;
import com.mtfm.deadman.notification.service.NotificationPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户端站内信推送桥接：委托 {@link NotificationPushService} 走 {@code client-inbox} 通道。
 */
@Component
@RequiredArgsConstructor
public class ClientInboxPushBridgeImpl implements ClientInboxPushBridge {

    private final NotificationPushService notificationPushService;

    /**
     * 向指定用户端用户推送站内信。
     *
     * @param clientUserId 用户端用户 ID
     * @param payload      推送载荷
     */
    @Override
    public void push(Long clientUserId, Map<String, Object> payload) {
        notificationPushService.pushToClient(clientUserId, payload);
    }
}
