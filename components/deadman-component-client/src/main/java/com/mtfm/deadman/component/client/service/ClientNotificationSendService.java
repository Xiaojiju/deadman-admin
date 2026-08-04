package com.mtfm.deadman.component.client.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.spi.ClientInboxPushBridge;
import com.mtfm.deadman.component.client.entity.ClientNotification;
import com.mtfm.deadman.component.client.entity.ClientNotificationRecipient;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.mapper.ClientNotificationMapper;
import com.mtfm.deadman.component.client.mapper.ClientUserBaseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * 用户端站内信广播发送服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationSendService {

    private static final int RECIPIENT_BATCH_SIZE = 500;

    /** WebSocket 消息类型：站内信通知 */
    public static final String MESSAGE_TYPE_INBOX = "INBOX_NOTIFICATION";

    private final ClientNotificationMapper clientNotificationMapper;
    private final ClientUserBaseMapper clientUserBaseMapper;
    private final ClientNotificationRecipientService recipientService;
    private final ObjectProvider<ClientInboxPushBridge> inboxPushBridgeProvider;
    private final JsonMapper jsonMapper;

    /**
     * 向全部正常用户广播站内信，并尝试 WebSocket 实时推送。
     *
     * @param title   标题
     * @param content 正文
     * @param bizType 业务类型
     * @param bizId   业务主键
     * @param extra   扩展字段（写入 extraJson 并随 WebSocket 推送）
     * @return 通知主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long broadcast(String title, String content, String bizType, Long bizId, Map<String, Object> extra) {
        List<Long> userIds = clientUserBaseMapper
                .selectList(new LambdaQueryWrapper<ClientUserBase>()
                        .eq(ClientUserBase::getStatus, UserStatus.ACTIVE.getValue())
                        .select(ClientUserBase::getId))
                .stream().map(ClientUserBase::getId).toList();

        ClientNotification notification = ClientNotification.builder().title(title.trim()).content(content.trim())
                .bizType(bizType).bizId(bizId).extraJson(serializeExtra(extra)).recipientCount(userIds.size()).build();
        clientNotificationMapper.insert(notification);

        List<ClientNotificationRecipient> recipients = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            recipients.add(recipientService.buildUnread(notification.getId(), userId));
        }
        recipientService.saveBatch(recipients, RECIPIENT_BATCH_SIZE);

        pushOnlineUsers(notification, extra);
        return notification.getId();
    }

    private void pushOnlineUsers(ClientNotification notification, Map<String, Object> extra) {
        ClientInboxPushBridge bridge = inboxPushBridgeProvider.getIfAvailable();
        if (bridge == null) {
            log.debug("未注册 ClientInboxPushBridge，跳过 WebSocket 推送 notificationId={}", notification.getId());
            return;
        }
        List<Long> userIds = recipientService.lambdaQuery()
                .eq(ClientNotificationRecipient::getNotificationId, notification.getId())
                .list().stream().map(ClientNotificationRecipient::getUserId).toList();
        Map<String, Object> payload = buildPushPayload(notification, extra);
        for (Long userId : userIds) {
            try {
                bridge.push(userId, payload);
            } catch (Exception ex) {
                log.warn("用户端站内信 WebSocket 推送失败 notificationId={} userId={}", notification.getId(), userId, ex);
            }
        }
    }

    private Map<String, Object> buildPushPayload(ClientNotification notification, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messageType", MESSAGE_TYPE_INBOX);
        payload.put("notificationId", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("bizType", notification.getBizType());
        payload.put("bizId", notification.getBizId());
        payload.put("createTime", notification.getCreateTime());
        if (extra != null && !extra.isEmpty()) {
            payload.put("extra", extra);
        }
        return payload;
    }

    private String serializeExtra(Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(extra);
        } catch (Exception ex) {
            throw new IllegalStateException("站内信扩展字段序列化失败", ex);
        }
    }
}
