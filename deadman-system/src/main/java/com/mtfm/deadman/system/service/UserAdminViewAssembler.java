package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.system.domain.department.DepartmentOperations;
import com.mtfm.deadman.system.domain.position.UserPositionOperations;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import com.mtfm.deadman.system.vo.org.UserPositionBindingVO;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final DepartmentOperations departmentOperations;
    private final UserPositionOperations userPositionOperations;

    /**
     * 批量组装用户列表摘要 VO。
     *
     * @param users 用户基础信息列表
     * @return 摘要 VO 列表
     */
    public List<UserAdminSummaryVO> assembleSummaries(List<UserBase> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = users.stream().map(UserBase::getId).toList();
        Map<Long, String> usernames = loadPrimaryUsernames(userIds);
        Map<Long, String> phones = userAccountService.loadPhonesByUserIds(userIds);
        Map<Long, List<String>> roleCodesMap = loadRoleCodesByUserIds(userIds);
        Map<Long, OrgRefVO> primaryDepartments = departmentOperations.loadPrimaryDepartmentRefsByUserIds(userIds);
        Map<Long, List<OrgRefVO>> departmentsMap = departmentOperations.loadDepartmentRefsByUserIds(userIds);
        Map<Long, List<UserPositionBindingVO>> positionBindingsMap =
                userPositionOperations.loadPositionBindingsByUserIds(userIds);

        return users.stream()
                .map(user -> toSummary(
                        user,
                        usernames.get(user.getId()),
                        phones.get(user.getId()),
                        primaryDepartments.get(user.getId()),
                        departmentsMap.getOrDefault(user.getId(), List.of()),
                        positionBindingsMap.getOrDefault(user.getId(), List.of()),
                        roleCodesMap.getOrDefault(user.getId(), List.of())))
                .toList();
    }

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
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<String>> result = new HashMap<>();
        for (SysUserRoleMapper.UserRoleCodeRow row : sysUserRoleMapper.selectRoleCodesByUserIds(userIds)) {
            result.computeIfAbsent(row.userId(), ignored -> new ArrayList<>()).add(row.roleCode());
        }
        result.replaceAll((userId, codes) -> codes.stream().sorted().toList());
        return result;
    }

    /**
     * 组装用户列表摘要 VO。
     */
    public UserAdminSummaryVO toSummary(
            UserBase user,
            String username,
            String phone,
            OrgRefVO primaryDepartment,
            List<OrgRefVO> departments,
            List<UserPositionBindingVO> positionBindings,
            List<String> roleCodes) {
        return new UserAdminSummaryVO(
                user.getId(),
                user.getUserCode(),
                username,
                user.getNickname(),
                user.getAvatar(),
                phone,
                primaryDepartment,
                departments,
                positionBindings,
                user.getStatus(),
                roleCodes,
                user.getCreateTime());
    }

    /**
     * 组装用户详情 VO。
     *
     * @param user 用户基础信息
     * @return 用户详情
     */
    public UserAdminDetailVO toDetail(UserBase user) {
        List<Long> userIds = List.of(user.getId());
        String username = loadPrimaryUsernames(userIds).get(user.getId());
        String phone = userAccountService.findPhoneByUserId(user.getId());
        List<String> roleCodes = loadRoleCodesByUserIds(userIds).getOrDefault(user.getId(), List.of());
        return new UserAdminDetailVO(
                user.getId(),
                user.getUserCode(),
                username,
                user.getNickname(),
                user.getAvatar(),
                phone,
                departmentOperations.loadPrimaryDepartmentRef(user.getId()),
                departmentOperations.loadDepartmentRefsByUserId(user.getId()),
                userPositionOperations.loadPositionBindingsByUserId(user.getId()),
                user.getStatus(),
                roleCodes,
                user.getCreateTime(),
                user.getUpdateTime());
    }
}
