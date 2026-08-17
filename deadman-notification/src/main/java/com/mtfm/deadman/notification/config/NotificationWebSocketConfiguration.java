package com.mtfm.deadman.notification.config;

import com.mtfm.deadman.notification.service.NotificationPushService;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 站内信 WebSocket 管道：管理端 {@code inbox} 与用户端 {@code client-inbox}。
 */
@Configuration
public class NotificationWebSocketConfiguration {

    /**
     * 管理端站内信通道，握手走 ADMIN 域 JWT。
     *
     * @param channelFactory 通道工厂
     * @return 管理端站内信通道
     */
    @Bean(NotificationPushService.INBOX_MESSAGE_CHANNEL)
    MessageChannel inboxMessageChannel(MessageChannelFactory channelFactory) {
        return channelFactory.create("inbox", "管理端站内信");
    }

    /**
     * 用户端站内信通道，握手走 CLIENT 域 JWT。
     *
     * @param channelFactory 通道工厂
     * @return 用户端站内信通道
     */
    @Bean(NotificationPushService.CLIENT_INBOX_MESSAGE_CHANNEL)
    MessageChannel clientInboxMessageChannel(MessageChannelFactory channelFactory) {
        return channelFactory.create("client-inbox", "用户端站内信");
    }
}
