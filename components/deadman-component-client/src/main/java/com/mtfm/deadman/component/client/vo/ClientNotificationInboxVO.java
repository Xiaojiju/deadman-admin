package com.mtfm.deadman.component.client.vo;

import java.time.LocalDateTime;

/**
 * 用户端站内信收件箱列表项。
 *
 * @param recipientId    收件记录主键
 * @param notificationId 通知主键
 * @param title          标题
 * @param content        正文
 * @param bizType        业务类型
 * @param bizId          业务主键
 * @param readStatus     阅读状态：0-未读 1-已读
 * @param readTime       阅读时间
 * @param createTime     接收时间
 */
public record ClientNotificationInboxVO(
        Long recipientId,
        Long notificationId,
        String title,
        String content,
        String bizType,
        Long bizId,
        Integer readStatus,
        LocalDateTime readTime,
        LocalDateTime createTime) {
}
