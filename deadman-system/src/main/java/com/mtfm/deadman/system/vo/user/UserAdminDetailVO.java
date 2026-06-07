package com.mtfm.deadman.system.vo.user;

import com.mtfm.deadman.system.vo.org.OrgRefVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户详情。
 *
 * @param id          用户 ID
 * @param userCode    用户编码
 * @param username    主登录用户名
 * @param nickname    昵称
 * @param avatar      头像 URL
 * @param phone       绑定手机号
 * @param department  所属部门
 * @param positions   职位列表
 * @param status      用户状态：0-禁用，1-正常
 * @param roleCodes   角色编码列表
 * @param createTime  创建时间
 * @param updateTime  更新时间
 */
public record UserAdminDetailVO(
        Long id,
        String userCode,
        String username,
        String nickname,
        String avatar,
        String phone,
        OrgRefVO department,
        List<OrgRefVO> positions,
        Integer status,
        List<String> roleCodes,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
