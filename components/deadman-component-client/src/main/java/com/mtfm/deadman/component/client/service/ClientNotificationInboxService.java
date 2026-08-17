package com.mtfm.deadman.component.client.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.dto.ClientNotificationInboxPageQuery;
import com.mtfm.deadman.component.client.entity.ClientNotification;
import com.mtfm.deadman.component.client.entity.ClientNotificationRecipient;
import com.mtfm.deadman.component.client.enums.ClientNotificationReadStatus;
import com.mtfm.deadman.component.client.mapper.ClientNotificationMapper;
import com.mtfm.deadman.component.client.vo.ClientNotificationInboxVO;

import lombok.RequiredArgsConstructor;

/**
 * 用户端站内信收件箱查询与已读标记。
 */
@Service
@RequiredArgsConstructor
public class ClientNotificationInboxService {

    private final ClientNotificationRecipientService recipientService;
    private final ClientNotificationMapper clientNotificationMapper;

    /**
     * 分页查询本人收件箱。
     *
     * @param userId 当前用户端用户 ID
     * @param query  查询条件
     * @return 分页列表
     */
    public PageVO<ClientNotificationInboxVO> pageInbox(Long userId, ClientNotificationInboxPageQuery query) {
        LambdaQueryWrapper<ClientNotificationRecipient> wrapper = new LambdaQueryWrapper<ClientNotificationRecipient>()
                .eq(ClientNotificationRecipient::getUserId, userId)
                .orderByDesc(ClientNotificationRecipient::getCreateTime);
        if (query.getReadStatus() != null) {
            wrapper.eq(ClientNotificationRecipient::getReadStatus, query.getReadStatus());
        }
        Page<ClientNotificationRecipient> page = recipientService.page(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageVO.of(List.of(), page.getTotal(), query);
        }

        List<Long> notificationIds = page.getRecords().stream()
                .map(ClientNotificationRecipient::getNotificationId)
                .distinct()
                .toList();
        Map<Long, ClientNotification> notificationMap = clientNotificationMapper.selectByIds(notificationIds).stream()
                .collect(Collectors.toMap(ClientNotification::getId, Function.identity()));

        List<ClientNotificationInboxVO> records = new ArrayList<>(page.getRecords().size());
        for (ClientNotificationRecipient recipient : page.getRecords()) {
            ClientNotification notification = notificationMap.get(recipient.getNotificationId());
            if (notification == null) {
                continue;
            }
            records.add(new ClientNotificationInboxVO(
                    recipient.getId(),
                    notification.getId(),
                    notification.getTitle(),
                    notification.getContent(),
                    notification.getBizType(),
                    notification.getBizId(),
                    recipient.getReadStatus(),
                    recipient.getReadTime(),
                    recipient.getCreateTime()));
        }
        return PageVO.of(records, page.getTotal(), query);
    }

    /**
     * 未读数量。
     *
     * @param userId 当前用户端用户 ID
     * @return 未读条数
     */
    public long countUnread(Long userId) {
        return recipientService.count(new LambdaQueryWrapper<ClientNotificationRecipient>()
                .eq(ClientNotificationRecipient::getUserId, userId)
                .eq(ClientNotificationRecipient::getReadStatus, ClientNotificationReadStatus.UNREAD.getValue()));
    }

    /**
     * 标记单条已读。
     *
     * @param userId      当前用户端用户 ID
     * @param recipientId 收件记录主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long recipientId) {
        ClientNotificationRecipient recipient = requireOwnedRecipient(userId, recipientId);
        if (recipient.getReadStatus() != null
                && recipient.getReadStatus() == ClientNotificationReadStatus.READ.getValue()) {
            return;
        }
        recipient.setReadStatus(ClientNotificationReadStatus.READ.getValue());
        recipient.setReadTime(LocalDateTime.now());
        recipientService.updateById(recipient);
    }

    /**
     * 全部标记已读。
     *
     * @param userId 当前用户端用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        List<ClientNotificationRecipient> unread = recipientService.list(
                new LambdaQueryWrapper<ClientNotificationRecipient>()
                        .eq(ClientNotificationRecipient::getUserId, userId)
                        .eq(ClientNotificationRecipient::getReadStatus, ClientNotificationReadStatus.UNREAD.getValue()));
        if (unread.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (ClientNotificationRecipient recipient : unread) {
            recipient.setReadStatus(ClientNotificationReadStatus.READ.getValue());
            recipient.setReadTime(now);
        }
        recipientService.updateBatchById(unread);
    }

    private ClientNotificationRecipient requireOwnedRecipient(Long userId, Long recipientId) {
        ClientNotificationRecipient recipient = recipientService.getById(recipientId);
        if (recipient == null || !userId.equals(recipient.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "站内信不存在");
        }
        return recipient;
    }
}
