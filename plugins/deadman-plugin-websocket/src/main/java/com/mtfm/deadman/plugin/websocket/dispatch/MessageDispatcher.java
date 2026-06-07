package com.mtfm.deadman.plugin.websocket.dispatch;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannelRegistry;
import com.mtfm.deadman.plugin.websocket.config.WebSocketPluginProperties;
import com.mtfm.deadman.plugin.websocket.entity.WsMessageRecord;
import com.mtfm.deadman.plugin.websocket.message.MessageDispatchRequest;
import com.mtfm.deadman.plugin.websocket.message.MessageSendStatus;
import com.mtfm.deadman.plugin.websocket.message.WsMessage;
import com.mtfm.deadman.plugin.websocket.service.WsMessageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 统一消息调度器：持久化、投递、重试与拦截回调。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDispatcher {

    private final MessageChannelRegistry channelRegistry;
    private final WsMessageRecordService messageRecordService;
    private final MessageTransport messageTransport;
    private final CompositeMessageDeliveryInterceptor deliveryInterceptor;
    private final WebSocketPluginProperties properties;
    private final JsonMapper jsonMapper;

    /**
     * 调度一条消息：先持久化再尝试投递。
     *
     * @param request 调度请求
     * @return 消息 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String dispatch(MessageDispatchRequest request) {
        channelRegistry.require(request.channelCode());

        String messageId = UUID.randomUUID().toString().replace("-", "");
        int maxRetry = request.maxRetry() != null ? request.maxRetry() : properties.getDispatch().getMaxRetry();

        WsMessageRecord record = WsMessageRecord.builder()
                .messageId(messageId)
                .channelCode(request.channelCode())
                .messageType(request.messageType())
                .targetUserKey(request.targetUserKey())
                .payloadJson(toJson(request.payload()))
                .status(MessageSendStatus.PENDING.getValue())
                .retryCount(0)
                .maxRetry(maxRetry)
                .build();
        messageRecordService.save(record);

        deliver(record);
        return messageId;
    }

    /**
     * 对已有记录执行投递（用于重试任务）。
     *
     * @param recordId 持久化记录主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void redeliver(Long recordId) {
        WsMessageRecord record = messageRecordService.getById(recordId);
        if (record == null) {
            return;
        }
        if (record.getStatus() == MessageSendStatus.SENT.getValue()
                || record.getStatus() == MessageSendStatus.FAILED.getValue()) {
            return;
        }
        deliver(record);
    }

    /**
     * 批量重试到期消息。
     */
    public void retryDueMessages() {
        List<WsMessageRecord> candidates = messageRecordService
                .listRetryCandidates(properties.getDispatch().getBatchSize());
        for (WsMessageRecord record : candidates) {
            try {
                redeliver(record.getId());
            } catch (Exception ex) {
                log.warn("消息重试异常 messageId={}", record.getMessageId(), ex);
            }
        }
    }

    private void deliver(WsMessageRecord record) {
        WsMessage message = toMessage(record);
        boolean sent = false;
        String error = null;
        try {
            sent = messageTransport.send(message);
            if (!sent) {
                error = "目标用户不在线或会话不可用";
            }
        } catch (Exception ex) {
            error = ex.getMessage();
            log.warn("消息投递异常 messageId={}", record.getMessageId(), ex);
        }

        if (sent) {
            markSent(record);
            notifyInterceptors(record, message, MessageSendStatus.SENT, true, null);
            return;
        }
        handleFailure(record, message, error);
    }

    private void handleFailure(WsMessageRecord record, WsMessage message, String error) {
        int nextRetryCount = record.getRetryCount() + 1;
        record.setRetryCount(nextRetryCount);
        record.setErrorMessage(truncate(error));

        if (nextRetryCount >= record.getMaxRetry()) {
            record.setStatus(MessageSendStatus.FAILED.getValue());
            record.setNextRetryTime(null);
            messageRecordService.updateById(record);
            notifyInterceptors(record, message, MessageSendStatus.FAILED, false, error);
            return;
        }

        record.setStatus(MessageSendStatus.RETRYING.getValue());
        record.setNextRetryTime(LocalDateTime.now().plus(properties.getDispatch().getRetryInterval()));
        messageRecordService.updateById(record);
    }

    private void markSent(WsMessageRecord record) {
        record.setStatus(MessageSendStatus.SENT.getValue());
        record.setErrorMessage(null);
        record.setNextRetryTime(null);
        messageRecordService.updateById(record);
    }

    private void notifyInterceptors(
            WsMessageRecord record,
            WsMessage message,
            MessageSendStatus status,
            boolean success,
            String error) {
        deliveryInterceptor.afterDelivery(MessageDeliveryContext.builder()
                .record(record)
                .message(message)
                .finalStatus(status)
                .success(success)
                .errorMessage(error)
                .build());
    }

    private WsMessage toMessage(WsMessageRecord record) {
        return WsMessage.builder()
                .messageId(record.getMessageId())
                .channelCode(record.getChannelCode())
                .messageType(record.getMessageType())
                .targetUserKey(record.getTargetUserKey())
                .payload(readPayload(record.getPayloadJson()))
                .createTime(record.getCreateTime())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readPayload(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return jsonMapper.readValue(json, Map.class);
        } catch (JacksonException ex) {
            return Map.of();
        }
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("消息负载序列化失败", ex);
        }
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() > 500 ? error.substring(0, 500) : error;
    }
}
