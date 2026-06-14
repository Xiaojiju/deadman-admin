package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端用户 VO 组装：账号、角色、组织等读模型聚合。
 */
@Component
@RequiredArgsConstructor
public class UserAdminViewAssembler {

    private final UserAccountService userAccountService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserOrgService userOrgService;
    private final UserPositionService userPositionService;

    /**
     * 批量加载主登录用户名（USERNAME 类型账号）。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到用户名的映射
     */
    public Map<Long, String> loadPrimaryUsernames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserAccount> accounts = userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                .in(UserAccount::getUserId, userIds)
                .eq(UserAccount::getAccountType, AccountType.USERNAME.getCode()));
        return accounts.stream()
                .collect(Collectors.toMap(
                        UserAccount::getUserId, UserAccount::getAccountIdentifier, (a, b) -> a));
    }

    /**
     * 批量加载用户角色编码。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到角色编码列表的映射
     */
    public Map<Long, List<String>> loadRoleCodesByUserIds(List<Long> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> sysUserRoleMapper.selectRoleCodesByUserId(id).stream().sorted().toList()));
    }

    /**
     * 组装用户列表摘要 VO。
     *
     * @param user       用户基础信息
     * @param username   主登录用户名
     * @param phone      手机号
     * @param department 部门引用
     * @param positions  职位引用列表
     * @param roleCodes  角色编码列表
     * @return 用户摘要 VO
     */
    public UserAdminSummaryVO toSummary(
            UserBase user,
            String username,
            String phone,
            OrgRefVO department,
            List<OrgRefVO> positions,
            List<String> roleCodes) {
        return new UserAdminSummaryVO(
                user.getId(),
                user.getUserCode(),
                username,
                user.getNickname(),
                user.getAvatar(),
                phone,
                department,
                positions,
                user.getStatus(),
                roleCodes,
                user.getCreateTime());
    }

    /**
     * 组装用户详情 VO。
     *
     * @param user 用户基础信息
     * @return 用户详情 VO
     */
    public UserAdminDetailVO toDetail(UserBase user) {
        String username = loadPrimaryUsernames(List.of(user.getId())).get(user.getId());
        String phone = userAccountService.findPhoneByUserId(user.getId());
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(user.getId());
        return new UserAdminDetailVO(
                user.getId(),
                user.getUserCode(),
                username,
                user.getNickname(),
                user.getAvatar(),
                phone,
                userOrgService.toDepartmentRef(user.getDepartmentId()),
                userPositionService.getPositionRefsByUserId(user.getId()),
                user.getStatus(),
                roleCodes,
                user.getCreateTime(),
                user.getUpdateTime());
    }
}
