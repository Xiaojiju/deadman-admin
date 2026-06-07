package com.mtfm.deadman.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 发送站内信请求。
 *
 * @param title          标题
 * @param content        正文
 * @param targetType     目标类型：1-用户 2-部门 3-职位 4-全体
 * @param userIds        定向用户 ID 列表
 * @param departmentIds  部门 ID 列表
 * @param positionIds    职位 ID 列表
 */
public record SendNotificationRequest(
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "内容不能为空") String content,
        @NotNull(message = "目标类型不能为空") Integer targetType,
        List<Long> userIds,
        List<Long> departmentIds,
        List<Long> positionIds) {
}
