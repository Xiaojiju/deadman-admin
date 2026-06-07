package com.mtfm.deadman.plugin.websocket.channel;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.websocket.config.WebSocketPluginProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息通道注册表，用于隔离不同用户体系的消息投递域。
 */
@Component
@RequiredArgsConstructor
public class MessageChannelRegistry {

    private final WebSocketPluginProperties properties;
    private final Map<String, MessageChannelDefinition> channels = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        for (WebSocketPluginProperties.Channel channel : properties.getChannels()) {
            register(new MessageChannelDefinition(channel.getCode(), channel.getDescription()));
        }
    }

    /**
     * 注册通道。
     *
     * @param definition 通道定义
     */
    public void register(MessageChannelDefinition definition) {
        channels.put(definition.code(), definition);
    }

    /**
     * 获取全部已注册通道。
     *
     * @return 通道列表
     */
    public Collection<MessageChannelDefinition> listAll() {
        return channels.values();
    }

    /**
     * 校验通道存在。
     *
     * @param channelCode 通道编码
     * @return 通道定义
     */
    public MessageChannelDefinition require(String channelCode) {
        MessageChannelDefinition definition = channels.get(channelCode);
        if (definition == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "未知消息通道: " + channelCode);
        }
        return definition;
    }

    /**
     * 判断通道是否已注册。
     *
     * @param channelCode 通道编码
     * @return 是否已注册
     */
    public boolean contains(String channelCode) {
        return channels.containsKey(channelCode);
    }
}
