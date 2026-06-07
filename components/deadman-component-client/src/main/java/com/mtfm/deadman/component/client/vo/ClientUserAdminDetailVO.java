package com.mtfm.deadman.component.client.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端用户管理详情。
 *
 * @param id          用户 ID
 * @param userCode    用户编码
 * @param username    主登录用户名
 * @param nickname    昵称
 * @param avatar      头像
 * @param phone       手机号
 * @param status      用户状态
 * @param accounts    绑定账号列表
 * @param createTime  创建时间
 * @param updateTime  更新时间
 */
public record ClientUserAdminDetailVO(
        Long id,
        String userCode,
        String username,
        String nickname,
        String avatar,
        String phone,
        Integer status,
        List<ClientUserAccountBindingVO> accounts,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
