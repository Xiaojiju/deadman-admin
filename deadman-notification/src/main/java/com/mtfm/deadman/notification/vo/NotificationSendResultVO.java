package com.mtfm.deadman.notification.vo;

/**
 * 发送站内信结果。
 *
 * @param notificationId  通知主键
 * @param recipientCount  实际投递用户数
 */
public record NotificationSendResultVO(Long notificationId, int recipientCount) {
}
