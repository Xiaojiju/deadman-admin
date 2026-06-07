package com.mtfm.deadman.plugin.websocket.channel;

import com.mtfm.deadman.plugin.websocket.dispatch.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 消息通道工厂：创建 {@link MessageChannel} Bean 并注册到 {@link MessageChannelRegistry}。
 */
@Component
@RequiredArgsConstructor
public class MessageChannelFactory {

    private final MessageChannelRegistry channelRegistry;
    private final MessageDispatcher messageDispatcher;

    /**
     * 创建并注册一条消息通道。
     *
     * @param code        通道编码，同时作为 WebSocket 路径段 {@code /ws/{code}}
     * @param description 通道说明
     * @return 可注入使用的通道 Bean
     */
    public MessageChannel create(String code, String description) {
        channelRegistry.register(new MessageChannelDefinition(code, description));
        return new MessageChannel(code, description, messageDispatcher);
    }
}
