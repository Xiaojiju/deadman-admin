package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.constants.SysRoleCodes;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.common.spi.UserRoleAssignment;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.system.dto.role.AssignUserRolesRequest;
import com.mtfm.deadman.system.dto.user.CreateUserRequest;
import com.mtfm.deadman.system.dto.user.ResetUserPasswordRequest;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.entity.SysUserRole;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.entity.UserPassword;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.util.UserCodeGenerator;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端用户：列表、新增、更新、停用、删除、角色关联。
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserBaseService userBaseService;
    private final UserService userService;
    private final UserAccountService userAccountService;
    private final UserPasswordService userPasswordService;
    private final UserRoleAssignment userRoleAssignment;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserAuthorityCache userAuthorityCache;
    private final DeadmanProperties deadmanProperties;
    private final UserOrgService userOrgService;
    private final UserPositionService userPositionService;

    /**
     * 分页查询用户列表。
     *
     * @param query 分页与筛选条件
     * @return 用户分页列表
     */
    public PageVO<UserAdminSummaryVO> pageUsers(UserAdminPageQuery query) {
        LambdaQueryWrapper<UserBase> wrapper = new LambdaQueryWrapper<UserBase>().orderByDesc(UserBase::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(UserBase::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            List<Long> accountMatchedIds = userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                            .in(
                                    UserAccount::getAccountType,
                                    AccountType.USERNAME.getCode(),
                                    AccountType.PHONE.getCode())
                            .like(UserAccount::getAccountIdentifier, kw))
                    .stream()
                    .map(UserAccount::getUserId)
                    .distinct()
                    .toList();
            wrapper.and(w -> {
                w.like(UserBase::getNickname, kw).or().like(UserBase::getUserCode, kw);
                if (!accountMatchedIds.isEmpty()) {
                    w.or().in(UserBase::getId, accountMatchedIds);
                }
            });
        }

        Page<UserBase> page = userBaseService.page(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        List<UserBase> records = page.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(List.of(), page.getTotal(), query);
        }

        List<Long> userIds = records.stream().map(UserBase::getId).toList();
        Map<Long, String> usernames = loadPrimaryUsernames(userIds);
        Map<Long, String> phones = userAccountService.loadPhonesByUserIds(userIds);
        Map<Long, List<String>> roleCodesMap = loadRoleCodesByUserIds(userIds);
        Map<Long, OrgRefVO> departmentRefs = userOrgService.loadDepartmentRefs(
                records.stream().map(UserBase::getDepartmentId).toList());
        Map<Long, List<OrgRefVO>> positionsMap = userPositionService.loadPositionRefsByUserIds(userIds);

        List<UserAdminSummaryVO> items = records.stream()
                .map(user -> toSummary(
                        user,
                        usernames.get(user.getId()),
                        phones.get(user.getId()),
                        departmentRefs.get(user.getDepartmentId()),
                        positionsMap.getOrDefault(user.getId(), List.of()),
                        roleCodesMap.getOrDefault(user.getId(), List.of())))
                .toList();
        return PageVO.of(items, page.getTotal(), query);
    }

    /**
     * 用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    public UserAdminDetailVO getUserDetail(Long userId) {
        UserBase user = userBaseService.requireById(userId);
        return toDetail(user);
    }

    /**
     * 新增用户并绑定默认 USER 角色。
     *
     * @param request 新增用户请求
     * @return 新建用户详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAdminDetailVO createUser(CreateUserRequest request) {
        if (userAccountService.existsUsername(request.username())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }
        if (StringUtils.hasText(request.phone()) && userAccountService.existsPhone(request.phone(), null)) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        UserOrgService.OrgAssignment org =
                userOrgService.resolveForCreate(request.departmentId(), request.positionIds());

        String userCode = UserCodeGenerator.generate(deadmanProperties.getUser().getUserCodePrefix());
        String nickname = StringUtils.hasText(request.nickname()) ? request.nickname() : request.username();

        UserBase userBase = UserBase.builder()
                .userCode(userCode)
                .nickname(nickname)
                .avatar(request.avatar())
                .departmentId(org.departmentId())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userBaseService.save(userBase);

        userPositionService.replaceUserPositions(userBase.getId(), org.departmentId(), org.positionIds());

        UserAccount account = UserAccount.builder()
                .userId(userBase.getId())
                .accountType(AccountType.USERNAME.getCode())
                .accountIdentifier(request.username())
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userAccountService.save(account);

        userAccountService.bindOrUpdatePhone(userBase.getId(), request.phone());
        userPasswordService.createPassword(userBase.getId(), request.password());
        userRoleAssignment.assignDefaultUserRole(userBase.getId());

        return getUserDetail(userBase.getId());
    }

    /**
     * 更新昵称、头像、状态、手机号、部门或职位。
     *
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 更新后的用户详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAdminDetailVO updateUser(Long userId, UpdateUserRequest request) {
        UserBase user = userBaseService.requireById(userId);
        if (request.status() != null && request.status() == UserStatus.DISABLED.getValue()) {
            assertNotSuperAdminUser(userId);
        }

        boolean changed = false;
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
            changed = true;
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
            changed = true;
        }
        if (request.status() != null) {
            validateStatus(request.status());
            user.setStatus(request.status());
            syncAccountStatus(userId, request.status());
            changed = true;
        }
        Long departmentId = user.getDepartmentId();
        if (request.departmentId() != null) {
            departmentId = request.departmentId();
            user.setDepartmentId(departmentId);
            changed = true;
        }
        if (request.positionIds() != null) {
            userPositionService.replaceUserPositions(userId, departmentId, request.positionIds());
            changed = true;
        } else if (request.departmentId() != null) {
            userOrgService.validatePositionsForDepartment(
                    departmentId, userPositionService.getPositionIdsByUserId(userId));
        }
        if (request.phone() != null) {
            userAccountService.bindOrUpdatePhone(userId, request.phone());
            changed = true;
        }

        if (changed) {
            userBaseService.updateById(user);
            userAuthorityCache.evictUserAuthorities(userId);
            evictProfileCache(user.getUserCode());
        }
        return getUserDetail(userId);
    }

    /**
     * 逻辑删除用户。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        UserBase user = userBaseService.requireById(userId);
        assertNotSuperAdminUser(userId);

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        userPositionService.removeByUserId(userId);
        userAccountService.remove(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
        userPasswordService.remove(new LambdaQueryWrapper<UserPassword>().eq(UserPassword::getUserId, userId));
        userBaseService.removeById(userId);

        userAuthorityCache.evictUserAuthorities(userId);
        evictProfileCache(user.getUserCode());
    }

    /**
     * 为用户分配角色（覆盖式）。
     *
     * @param userId  用户 ID
     * @param request 角色 ID 列表
     * @return 更新后的用户详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAdminDetailVO assignUserRoles(Long userId, AssignUserRolesRequest request) {
        userRoleAssignment.assignUserRoles(userId, request.roleIds());
        return getUserDetail(userId);
    }

    /**
     * 管理端重置用户密码（用于用户忘记密码等场景）。
     *
     * @param userId  用户 ID
     * @param request 新密码请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId, ResetUserPasswordRequest request) {
        UserBase user = userBaseService.requireById(userId);
        userPasswordService.resetPassword(userId, request.newPassword());
        evictProfileCache(user.getUserCode());
    }

    private UserAdminDetailVO toDetail(UserBase user) {
        String username = loadPrimaryUsername(user.getId());
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

    private void assertNotSuperAdminUser(Long userId) {
        if (sysUserRoleMapper.selectRoleCodesByUserId(userId).contains(SysRoleCodes.SUPER_ADMIN)) {
            throw new BusinessException(ResultCode.USER_SUPER_ADMIN_PROTECTED);
        }
    }

    private void evictProfileCache(String userCode) {
        if (StringUtils.hasText(userCode)) {
            userService.evictProfileCache(userCode);
        }
    }

    private void validateStatus(Integer status) {
        if (status != UserStatus.ACTIVE.getValue() && status != UserStatus.DISABLED.getValue()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态仅支持 0-禁用 或 1-正常");
        }
    }

    private void syncAccountStatus(Long userId, Integer status) {
        List<UserAccount> accounts = userAccountService.list(
                new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
        for (UserAccount account : accounts) {
            account.setStatus(status);
        }
        if (!accounts.isEmpty()) {
            userAccountService.updateBatchById(accounts);
        }
    }

    private String loadPrimaryUsername(Long userId) {
        return loadPrimaryUsernames(List.of(userId)).getOrDefault(userId, null);
    }

    private Map<Long, String> loadPrimaryUsernames(List<Long> userIds) {
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

    private Map<Long, List<String>> loadRoleCodesByUserIds(List<Long> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> sysUserRoleMapper.selectRoleCodesByUserId(id).stream().sorted().toList()));
    }

    private UserAdminSummaryVO toSummary(
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
}
