package com.mtfm.deadman.plugin.websocket.channel;

import com.mtfm.deadman.plugin.websocket.dispatch.MessageDispatcher;
import com.mtfm.deadman.plugin.websocket.message.MessageDispatchRequest;
import lombok.Getter;

import java.util.Map;

/**
 * 消息通道 Bean：封装通道编码与统一调度入口，业务通过 Spring 注册后注入使用。
 */
@Getter
public class MessageChannel {

    private final String code;
    private final String description;
    private final MessageDispatcher dispatcher;

    MessageChannel(String code, String description, MessageDispatcher dispatcher) {
        this.code = code;
        this.description = description;
        this.dispatcher = dispatcher;
    }

    /**
     * 向通道内目标用户投递消息。
     *
     * @param messageType   消息类型
     * @param targetUserKey 目标用户在通道内的标识
     * @param payload       扩展负载
     * @return 消息 ID
     */
    public String dispatch(String messageType, String targetUserKey, Map<String, Object> payload) {
        return dispatch(messageType, targetUserKey, payload, null);
    }

    /**
     * 向通道内目标用户投递消息。
     *
     * @param messageType   消息类型
     * @param targetUserKey 目标用户在通道内的标识
     * @param payload       扩展负载
     * @param maxRetry      最大重试次数，null 使用全局配置
     * @return 消息 ID
     */
    public String dispatch(String messageType, String targetUserKey, Map<String, Object> payload, Integer maxRetry) {
        return dispatcher.dispatch(new MessageDispatchRequest(code, messageType, targetUserKey, payload, maxRetry));
    }
}
