package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.mtfm.deadman.system.entity.SysUserRole;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.entity.UserPassword;
import com.mtfm.deadman.common.event.user.UserCreatedEvent;
import com.mtfm.deadman.common.event.user.UserCreationSource;
import com.mtfm.deadman.common.event.user.UserDeletedEvent;
import com.mtfm.deadman.common.event.user.UserUpdatedEvent;
import com.mtfm.deadman.system.mapper.SysUserRoleMapper;
import com.mtfm.deadman.system.util.UserCodeGenerator;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理端用户写操作：创建、更新、删除、密码重置与角色分配。
 */
@Service
@RequiredArgsConstructor
public class UserAdminMutationService {

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
    private final UserAdminQueryService userAdminQueryService;
    private final UserAdminGuard userAdminGuard;
    private final ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(new UserCreatedEvent(userBase.getId(), UserCreationSource.ADMIN));

        return userAdminQueryService.getUserDetail(userBase.getId());
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
            userAdminGuard.assertNotSuperAdminUser(userId);
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
        boolean departmentChanged = false;
        if (request.departmentId() != null) {
            departmentId = request.departmentId();
            user.setDepartmentId(departmentId);
            changed = true;
            departmentChanged = true;
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
            eventPublisher.publishEvent(new UserUpdatedEvent(userId, departmentChanged));
        }
        return userAdminQueryService.getUserDetail(userId);
    }

    /**
     * 逻辑删除用户。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        UserBase user = userBaseService.requireById(userId);
        userAdminGuard.assertNotSuperAdminUser(userId);

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        userPositionService.removeByUserId(userId);
        userAccountService.remove(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
        userPasswordService.remove(new LambdaQueryWrapper<UserPassword>().eq(UserPassword::getUserId, userId));
        userBaseService.removeById(userId);

        userAuthorityCache.evictUserAuthorities(userId);
        evictProfileCache(user.getUserCode());
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
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
        return userAdminQueryService.getUserDetail(userId);
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
}
