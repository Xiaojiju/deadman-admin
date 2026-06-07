package com.mtfm.deadman.notification.vo;

import java.time.LocalDateTime;

/**
 * 已发送通知摘要。
 *
 * @param id              通知主键
 * @param title           标题
 * @param content         正文
 * @param targetType      目标类型
 * @param recipientCount  投递人数
 * @param senderUserId    发送人
 * @param createTime      发送时间
 */
public record NotificationSentVO(
        Long id,
        String title,
        String content,
        Integer targetType,
        Integer recipientCount,
        Long senderUserId,
        LocalDateTime createTime) {
}
