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
import com.mtfm.deadman.component.client.enums.ClientNotificationCategory;
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
        applyCategory(wrapper, query.getCategory());
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
            boolean read = recipient.getReadStatus() != null
                    && recipient.getReadStatus() == ClientNotificationReadStatus.READ.getValue();
            records.add(new ClientNotificationInboxVO(
                    recipient.getId(),
                    notification.getId(),
                    notification.getTitle(),
                    notification.getContent(),
                    notification.getBizType(),
                    notification.getBizId(),
                    recipient.getReadStatus(),
                    recipient.getReadTime(),
                    recipient.getCreateTime(),
                    recipient.getId(),
                    categoryOf(notification),
                    read));
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
        return countUnread(userId, null);
    }

    /**
     * 按分类统计未读数量。分类为空时统计全部。
     *
     * @param userId 当前用户端用户 ID
     * @param category 分类，空表示全部
     * @return 未读条数
     */
    public long countUnread(Long userId, String category) {
        LambdaQueryWrapper<ClientNotificationRecipient> wrapper = new LambdaQueryWrapper<ClientNotificationRecipient>()
                .eq(ClientNotificationRecipient::getUserId, userId)
                .eq(ClientNotificationRecipient::getReadStatus, ClientNotificationReadStatus.UNREAD.getValue());
        applyCategory(wrapper, category);
        return recipientService.count(wrapper);
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

    private static void applyCategory(LambdaQueryWrapper<ClientNotificationRecipient> wrapper, String category) {
        ClientNotificationCategory parsed = ClientNotificationCategory.from(category);
        if (parsed == null) {
            return;
        }
        wrapper.inSql(ClientNotificationRecipient::getNotificationId,
                "SELECT id FROM client_notification WHERE category = '" + parsed.name() + "'");
    }

    private static String categoryOf(ClientNotification notification) {
        if (notification.getCategory() == null || notification.getCategory().isBlank()) {
            return ClientNotificationCategory.SYSTEM.name();
        }
        return notification.getCategory();
    }

    private ClientNotificationRecipient requireOwnedRecipient(Long userId, Long recipientId) {
        ClientNotificationRecipient recipient = recipientService.getById(recipientId);
        if (recipient == null || !userId.equals(recipient.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "站内信不存在");
        }
        return recipient;
    }
}
