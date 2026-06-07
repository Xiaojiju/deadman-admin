package com.mtfm.deadman.component.client.vo;

import java.time.LocalDateTime;

/**
 * 用户端用户管理列表项。
 *
 * @param id         用户 ID
 * @param userCode   用户编码
 * @param username   主登录用户名
 * @param nickname   昵称
 * @param avatar     头像
 * @param phone      手机号
 * @param status     用户状态
 * @param createTime 创建时间
 */
public record ClientUserAdminSummaryVO(
        Long id,
        String userCode,
        String username,
        String nickname,
        String avatar,
        String phone,
        Integer status,
        LocalDateTime createTime) {
}
