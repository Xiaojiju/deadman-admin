package com.mtfm.deadman.notification.controller;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.notification.dto.NotificationSentPageQuery;
import com.mtfm.deadman.notification.dto.SendNotificationRequest;
import com.mtfm.deadman.notification.service.NotificationSendService;
import com.mtfm.deadman.notification.vo.NotificationSendResultVO;
import com.mtfm.deadman.notification.vo.NotificationSentVO;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.SecurityAuthSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内信发送与管理端查询接口。
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSendController {

    private final NotificationSendService notificationSendService;

    /**
     * 发送站内信（定向 / 部门 / 职位 / 全体）。
     *
     * @param loginUser 当前登录用户
     * @param request   发送请求
     * @return 发送结果
     */
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('notification:send')")
    public Result<NotificationSendResultVO> send(
            @AuthenticationPrincipal LoginUser loginUser, @Valid @RequestBody SendNotificationRequest request) {
        LoginUser user = SecurityAuthSupport.requireLogin(loginUser);
        return Result.ok(notificationSendService.send(user.getUserId(), request));
    }

    /**
     * 分页查询已发送站内信。
     *
     * @param query 查询条件
     * @return 分页列表
     */
    @GetMapping("/sent")
    @PreAuthorize("hasAuthority('notification:sent:read')")
    public Result<PageVO<NotificationSentVO>> pageSent(@Valid NotificationSentPageQuery query) {
        return Result.ok(notificationSendService.pageSent(query));
    }
}
