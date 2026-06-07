package com.mtfm.deadman.notification.config;

import com.mtfm.deadman.notification.service.NotificationPushService;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * deadman-admin 站内信 WebSocket 管道。
 */
@Configuration
public class NotificationWebSocketConfiguration {

    @Bean(NotificationPushService.INBOX_MESSAGE_CHANNEL)
    MessageChannel inboxMessageChannel(MessageChannelFactory channelFactory) {
        return channelFactory.create("inbox", "管理端站内信");
    }
}
