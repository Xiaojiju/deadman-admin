package com.mtfm.deadman.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.notification.dto.NotificationInboxPageQuery;
import com.mtfm.deadman.notification.entity.SysNotification;
import com.mtfm.deadman.notification.entity.SysNotificationRecipient;
import com.mtfm.deadman.notification.enums.NotificationReadStatus;
import com.mtfm.deadman.notification.mapper.SysNotificationMapper;
import com.mtfm.deadman.notification.vo.NotificationInboxVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户收件箱查询与已读标记。
 */
@Service
@RequiredArgsConstructor
public class NotificationInboxService {

    private final SysNotificationRecipientService recipientService;
    private final SysNotificationMapper sysNotificationMapper;

    /**
     * 分页查询本人收件箱。
     *
     * @param userId 当前用户
     * @param query  查询条件
     * @return 分页列表
     */
    public PageVO<NotificationInboxVO> pageInbox(Long userId, NotificationInboxPageQuery query) {
        LambdaQueryWrapper<SysNotificationRecipient> wrapper = new LambdaQueryWrapper<SysNotificationRecipient>()
                .eq(SysNotificationRecipient::getUserId, userId)
                .orderByDesc(SysNotificationRecipient::getCreateTime);
        if (query.getReadStatus() != null) {
            wrapper.eq(SysNotificationRecipient::getReadStatus, query.getReadStatus());
        }
        Page<SysNotificationRecipient> page = recipientService.page(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageVO.of(List.of(), page.getTotal(), query);
        }

        List<Long> notificationIds = page.getRecords().stream()
                .map(SysNotificationRecipient::getNotificationId)
                .distinct()
                .toList();
        Map<Long, SysNotification> notificationMap = sysNotificationMapper.selectByIds(notificationIds).stream()
                .collect(Collectors.toMap(SysNotification::getId, Function.identity()));

        List<NotificationInboxVO> records = new ArrayList<>(page.getRecords().size());
        for (SysNotificationRecipient recipient : page.getRecords()) {
            SysNotification notification = notificationMap.get(recipient.getNotificationId());
            if (notification == null) {
                continue;
            }
            records.add(new NotificationInboxVO(
                    recipient.getId(),
                    notification.getId(),
                    notification.getTitle(),
                    notification.getContent(),
                    recipient.getReadStatus(),
                    recipient.getReadTime(),
                    recipient.getCreateTime()));
        }
        return PageVO.of(records, page.getTotal(), query);
    }

    /**
     * 未读数量。
     *
     * @param userId 当前用户
     * @return 未读条数
     */
    public long countUnread(Long userId) {
        return recipientService.count(new LambdaQueryWrapper<SysNotificationRecipient>()
                .eq(SysNotificationRecipient::getUserId, userId)
                .eq(SysNotificationRecipient::getReadStatus, NotificationReadStatus.UNREAD.getValue()));
    }

    /**
     * 标记单条已读。
     *
     * @param userId      当前用户
     * @param recipientId 收件记录主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long recipientId) {
        SysNotificationRecipient recipient = requireOwnedRecipient(userId, recipientId);
        if (recipient.getReadStatus() == NotificationReadStatus.READ.getValue()) {
            return;
        }
        recipient.setReadStatus(NotificationReadStatus.READ.getValue());
        recipient.setReadTime(LocalDateTime.now());
        recipientService.updateById(recipient);
    }

    /**
     * 全部标记已读。
     *
     * @param userId 当前用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        List<SysNotificationRecipient> unread = recipientService.list(new LambdaQueryWrapper<SysNotificationRecipient>()
                .eq(SysNotificationRecipient::getUserId, userId)
                .eq(SysNotificationRecipient::getReadStatus, NotificationReadStatus.UNREAD.getValue()));
        if (unread.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (SysNotificationRecipient recipient : unread) {
            recipient.setReadStatus(NotificationReadStatus.READ.getValue());
            recipient.setReadTime(now);
        }
        recipientService.updateBatchById(unread);
    }

    private SysNotificationRecipient requireOwnedRecipient(Long userId, Long recipientId) {
        SysNotificationRecipient recipient = recipientService.getById(recipientId);
        if (recipient == null || !userId.equals(recipient.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "站内信不存在");
        }
        return recipient;
    }
}
