package com.mtfm.deadman.notification.controller;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.notification.dto.NotificationInboxPageQuery;
import com.mtfm.deadman.notification.service.NotificationInboxService;
import com.mtfm.deadman.notification.vo.NotificationInboxVO;
import com.mtfm.deadman.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内信收件箱接口。
 */
@RestController
@RequestMapping("/api/notifications/inbox")
@RequiredArgsConstructor
public class NotificationInboxController {

    private final NotificationInboxService notificationInboxService;

    /**
     * 分页查询本人收件箱。
     *
     * @param loginUser 当前用户
     * @param query     查询条件
     * @return 分页列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notification:inbox:read')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<PageVO<NotificationInboxVO>> pageInbox(
            @AuthenticationPrincipal LoginUser loginUser, @Valid NotificationInboxPageQuery query) {
        return Result.ok(notificationInboxService.pageInbox(loginUser.getUserId(), query));
    }

    /**
     * 未读数量。
     *
     * @param loginUser 当前用户
     * @return 未读条数
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('notification:inbox:read')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<Long> unreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(notificationInboxService.countUnread(loginUser.getUserId()));
    }

    /**
     * 标记单条已读。
     *
     * @param loginUser   当前用户
     * @param recipientId 收件记录主键
     * @return 空
     */
    @PostMapping("/{recipientId}/read")
    @PreAuthorize("hasAuthority('notification:inbox:update')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<Void> markRead(
            @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long recipientId) {
        notificationInboxService.markRead(loginUser.getUserId(), recipientId);
        return Result.ok();
    }

    /**
     * 全部标记已读。
     *
     * @param loginUser 当前用户
     * @return 空
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('notification:inbox:update')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<Void> markAllRead(@AuthenticationPrincipal LoginUser loginUser) {
        notificationInboxService.markAllRead(loginUser.getUserId());
        return Result.ok();
    }
}
