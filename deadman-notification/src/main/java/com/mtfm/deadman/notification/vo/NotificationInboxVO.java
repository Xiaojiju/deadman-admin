package com.mtfm.deadman.notification.vo;

import java.time.LocalDateTime;

/**
 * 收件箱列表项。
 *
 * @param recipientId    收件记录主键
 * @param notificationId 通知主键
 * @param title          标题
 * @param content        正文摘要（列表可截断展示）
 * @param readStatus     阅读状态
 * @param readTime       阅读时间
 * @param createTime     接收时间
 */
public record NotificationInboxVO(
        Long recipientId,
        Long notificationId,
        String title,
        String content,
        Integer readStatus,
        LocalDateTime readTime,
        LocalDateTime createTime) {
}
