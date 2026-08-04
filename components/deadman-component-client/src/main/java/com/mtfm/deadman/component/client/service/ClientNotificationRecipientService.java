package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.component.client.entity.ClientNotificationRecipient;
import com.mtfm.deadman.component.client.enums.ClientNotificationReadStatus;
import com.mtfm.deadman.component.client.mapper.ClientNotificationRecipientMapper;
import org.springframework.stereotype.Service;

/**
 * 用户端站内信收件人持久化服务。
 */
@Service
public class ClientNotificationRecipientService
    extends ServiceImpl<ClientNotificationRecipientMapper, ClientNotificationRecipient> {

    /**
     * 构建未读收件记录。
     *
     * @param notificationId 通知主键
     * @param userId 收件人用户 ID
     * @return 收件记录
     */
    public ClientNotificationRecipient buildUnread(Long notificationId, Long userId) {
        return ClientNotificationRecipient.builder().notificationId(notificationId).userId(userId)
            .readStatus(ClientNotificationReadStatus.UNREAD.getValue()).build();
    }
}
