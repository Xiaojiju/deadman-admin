package com.mtfm.deadman.notification.service;

import com.mtfm.deadman.notification.entity.SysNotification;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通过站内信 WebSocket 管道推送实时通知（管理端 {@code inbox} / 用户端 {@code client-inbox}）。
 */
@Slf4j
@Service
public class NotificationPushService {

    /** 管理端站内信通道 Bean 名，对应路径 {@code /ws/inbox} */
    public static final String INBOX_MESSAGE_CHANNEL = "inboxMessageChannel";

    /** 用户端站内信通道 Bean 名，对应路径 {@code /ws/client-inbox} */
    public static final String CLIENT_INBOX_MESSAGE_CHANNEL = "clientInboxMessageChannel";

    /** WebSocket 消息类型：站内信通知 */
    public static final String MESSAGE_TYPE_INBOX = "INBOX_NOTIFICATION";

    private final MessageChannel inboxMessageChannel;
    private final MessageChannel clientInboxMessageChannel;

    public NotificationPushService(
            @Qualifier(INBOX_MESSAGE_CHANNEL) MessageChannel inboxMessageChannel,
            @Qualifier(CLIENT_INBOX_MESSAGE_CHANNEL) MessageChannel clientInboxMessageChannel) {
        this.inboxMessageChannel = inboxMessageChannel;
        this.clientInboxMessageChannel = clientInboxMessageChannel;
    }

    /**
     * 向管理端在线用户推送站内信（{@code inbox} 通道）。
     *
     * @param notification 通知主记录
     * @param userIds      收件人用户 ID（系统用户）
     */
    public void push(SysNotification notification, Set<Long> userIds) {
        for (Long userId : userIds) {
            try {
                inboxMessageChannel.dispatch(
                        MESSAGE_TYPE_INBOX,
                        String.valueOf(userId),
                        buildAdminPayload(notification));
            } catch (Exception ex) {
                log.warn("管理端站内信 WebSocket 推送失败 notificationId={} userId={}", notification.getId(), userId, ex);
            }
        }
    }

    /**
     * 向用户端在线用户推送站内信（{@code client-inbox} 通道）。
     *
     * @param clientUserId 用户端用户 ID
     * @param payload      推送载荷
     */
    public void pushToClient(Long clientUserId, Map<String, Object> payload) {
        if (clientUserId == null) {
            return;
        }
        clientInboxMessageChannel.dispatch(MESSAGE_TYPE_INBOX, String.valueOf(clientUserId), payload);
    }

    private Map<String, Object> buildAdminPayload(SysNotification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("createTime", notification.getCreateTime());
        return payload;
    }
}
