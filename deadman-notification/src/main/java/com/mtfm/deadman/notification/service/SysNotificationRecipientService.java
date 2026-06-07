package com.mtfm.deadman.notification.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.notification.entity.SysNotificationRecipient;
import com.mtfm.deadman.notification.enums.NotificationReadStatus;
import com.mtfm.deadman.notification.mapper.SysNotificationRecipientMapper;
import org.springframework.stereotype.Service;

/**
 * 站内信收件人持久化服务。
 */
@Service
public class SysNotificationRecipientService
        extends ServiceImpl<SysNotificationRecipientMapper, SysNotificationRecipient> {

    /**
     * 构建未读收件记录。
     *
     * @param notificationId 通知主键
     * @param userId         用户主键
     * @return 收件记录
     */
    public SysNotificationRecipient buildUnread(Long notificationId, Long userId) {
        return SysNotificationRecipient.builder()
                .notificationId(notificationId)
                .userId(userId)
                .readStatus(NotificationReadStatus.UNREAD.getValue())
                .build();
    }
}
