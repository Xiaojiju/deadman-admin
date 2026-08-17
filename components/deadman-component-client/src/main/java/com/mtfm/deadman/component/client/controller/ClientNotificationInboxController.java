package com.mtfm.deadman.component.client.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.dto.ClientNotificationInboxPageQuery;
import com.mtfm.deadman.component.client.service.ClientNotificationInboxService;
import com.mtfm.deadman.component.client.vo.ClientNotificationInboxVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户端站内信收件箱接口（CLIENT 域 JWT）。
 * <p>
 * 实时推送走 WebSocket：{@code ws://{host}/ws/client-inbox?token={clientJwt}}。
 */
@RestController
@RequestMapping("/client/api/notifications/inbox")
@RequiredArgsConstructor
public class ClientNotificationInboxController {

    private final ClientNotificationInboxService clientNotificationInboxService;

    /**
     * 分页查询本人收件箱。
     *
     * @param loginUser 当前用户
     * @param query     查询条件
     * @return 分页列表
     */
    @GetMapping
    @RequireAuth(AuthRealm.CLIENT)
    public Result<PageVO<ClientNotificationInboxVO>> pageInbox(
            @AuthenticationPrincipal ClientLoginUser loginUser, @Valid ClientNotificationInboxPageQuery query) {
        return Result.ok(clientNotificationInboxService.pageInbox(loginUser.getUserId(), query));
    }

    /**
     * 未读数量。
     *
     * @param loginUser 当前用户
     * @return 未读条数
     */
    @GetMapping("/unread-count")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<Long> unreadCount(@AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(clientNotificationInboxService.countUnread(loginUser.getUserId()));
    }

    /**
     * 标记单条已读。
     *
     * @param loginUser   当前用户
     * @param recipientId 收件记录主键
     * @return 空
     */
    @PostMapping("/{recipientId}/read")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<Void> markRead(
            @AuthenticationPrincipal ClientLoginUser loginUser, @PathVariable Long recipientId) {
        clientNotificationInboxService.markRead(loginUser.getUserId(), recipientId);
        return Result.ok();
    }

    /**
     * 全部标记已读。
     *
     * @param loginUser 当前用户
     * @return 空
     */
    @PostMapping("/read-all")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<Void> markAllRead(@AuthenticationPrincipal ClientLoginUser loginUser) {
        clientNotificationInboxService.markAllRead(loginUser.getUserId());
        return Result.ok();
    }
}
