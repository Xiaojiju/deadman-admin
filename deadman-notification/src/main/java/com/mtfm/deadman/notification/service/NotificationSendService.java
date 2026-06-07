package com.mtfm.deadman.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.notification.dto.NotificationSentPageQuery;
import com.mtfm.deadman.notification.dto.SendNotificationRequest;
import com.mtfm.deadman.notification.entity.SysNotification;
import com.mtfm.deadman.notification.entity.SysNotificationRecipient;
import com.mtfm.deadman.notification.mapper.SysNotificationMapper;
import com.mtfm.deadman.notification.vo.NotificationSendResultVO;
import com.mtfm.deadman.notification.vo.NotificationSentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 站内信发送与管理端查询。
 */
@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private static final int RECIPIENT_BATCH_SIZE = 500;

    private final SysNotificationMapper sysNotificationMapper;
    private final SysNotificationRecipientService recipientService;
    private final NotificationTargetResolver targetResolver;
    private final NotificationPushService pushService;
    private final JsonMapper jsonMapper;

    /**
     * 发送站内信：持久化后推送 WebSocket。
     *
     * @param senderUserId 发送人
     * @param request        发送请求
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public NotificationSendResultVO send(Long senderUserId, SendNotificationRequest request) {
        Set<Long> userIds = targetResolver.resolveUserIds(request);

        SysNotification notification = SysNotification.builder()
                .title(request.title().trim())
                .content(request.content().trim())
                .targetType(request.targetType())
                .targetPayloadJson(buildTargetPayloadJson(request))
                .senderUserId(senderUserId)
                .recipientCount(userIds.size())
                .build();
        sysNotificationMapper.insert(notification);

        List<SysNotificationRecipient> recipients = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            recipients.add(recipientService.buildUnread(notification.getId(), userId));
        }
        recipientService.saveBatch(recipients, RECIPIENT_BATCH_SIZE);

        pushService.push(notification, userIds);
        return new NotificationSendResultVO(notification.getId(), userIds.size());
    }

    /**
     * 分页查询已发送通知。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public PageVO<NotificationSentVO> pageSent(NotificationSentPageQuery query) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<SysNotification>()
                .orderByDesc(SysNotification::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysNotification::getTitle, query.getKeyword().trim());
        }
        Page<SysNotification> page = sysNotificationMapper.selectPage(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        List<NotificationSentVO> records = page.getRecords().stream()
                .map(item -> new NotificationSentVO(
                        item.getId(),
                        item.getTitle(),
                        item.getContent(),
                        item.getTargetType(),
                        item.getRecipientCount(),
                        item.getSenderUserId(),
                        item.getCreateTime()))
                .toList();
        return PageVO.of(records, page.getTotal(), query);
    }

    private String buildTargetPayloadJson(SendNotificationRequest request) {
        Map<String, Object> payload = Map.of(
                "userIds", request.userIds() == null ? List.of() : request.userIds(),
                "departmentIds", request.departmentIds() == null ? List.of() : request.departmentIds(),
                "positionIds", request.positionIds() == null ? List.of() : request.positionIds());
        try {
            return jsonMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("目标参数序列化失败", ex);
        }
    }
}
