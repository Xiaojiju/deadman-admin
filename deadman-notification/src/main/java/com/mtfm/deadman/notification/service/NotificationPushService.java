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
 * 通过站内信 WebSocket 管道推送实时通知。
 */
@Slf4j
@Service
public class NotificationPushService {

    public static final String INBOX_MESSAGE_CHANNEL = "inboxMessageChannel";
    private static final String MESSAGE_TYPE_INBOX = "INBOX_NOTIFICATION";

    private final MessageChannel inboxMessageChannel;

    public NotificationPushService(
            @Qualifier(INBOX_MESSAGE_CHANNEL) MessageChannel inboxMessageChannel) {
        this.inboxMessageChannel = inboxMessageChannel;
    }

    /**
     * 向在线用户推送站内信。
     *
     * @param notification 通知主记录
     * @param userIds      收件人用户 ID
     */
    public void push(SysNotification notification, Set<Long> userIds) {
        for (Long userId : userIds) {
            try {
                inboxMessageChannel.dispatch(
                        MESSAGE_TYPE_INBOX,
                        String.valueOf(userId),
                        buildPayload(notification));
            } catch (Exception ex) {
                log.warn("站内信 WebSocket 推送失败 notificationId={} userId={}", notification.getId(), userId, ex);
            }
        }
    }

    private Map<String, Object> buildPayload(SysNotification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("createTime", notification.getCreateTime());
        return payload;
    }
}
