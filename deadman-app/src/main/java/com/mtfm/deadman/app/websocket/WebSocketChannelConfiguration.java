package com.mtfm.deadman.app.websocket;

import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket 消息通道 Bean 定义：引入插件后在此声明各用户体系通道。
 */
@Configuration
public class WebSocketChannelConfiguration {

    public static final String ADMIN_MESSAGE_CHANNEL = "adminMessageChannel";
    public static final String MOBILE_MESSAGE_CHANNEL = "mobileMessageChannel";

    @Bean(ADMIN_MESSAGE_CHANNEL)
    MessageChannel adminMessageChannel(MessageChannelFactory channelFactory) {
        return channelFactory.create("admin", "管理端系统用户体系");
    }

    @Bean(MOBILE_MESSAGE_CHANNEL)
    MessageChannel mobileMessageChannel(MessageChannelFactory channelFactory) {
        return channelFactory.create("mobile", "移动端用户体系");
    }
}
