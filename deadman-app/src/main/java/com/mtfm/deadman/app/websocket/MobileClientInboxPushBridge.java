package com.mtfm.deadman.app.websocket;

import com.mtfm.deadman.common.spi.ClientInboxPushBridge;
import com.mtfm.deadman.component.client.service.ClientNotificationSendService;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户端站内信 WebSocket 推送桥接（mobile 通道）。
 */
@Component
public class MobileClientInboxPushBridge implements ClientInboxPushBridge {

    private final MessageChannel mobileMessageChannel;

    public MobileClientInboxPushBridge(
        @Qualifier(WebSocketChannelConfiguration.MOBILE_MESSAGE_CHANNEL) MessageChannel mobileMessageChannel) {
        this.mobileMessageChannel = mobileMessageChannel;
    }

    @Override
    public void push(Long clientUserId, Map<String, Object> payload) {
        mobileMessageChannel.dispatch(ClientNotificationSendService.MESSAGE_TYPE_INBOX, String.valueOf(clientUserId),
            payload);
    }
}
