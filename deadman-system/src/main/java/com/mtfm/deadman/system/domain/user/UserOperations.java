package com.mtfm.deadman.system.domain.user;

import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.event.user.UserCreatedEvent;
import com.mtfm.deadman.common.event.user.UserCreationSource;
import com.mtfm.deadman.common.event.user.UserDeletedEvent;
import com.mtfm.deadman.common.event.user.UserUpdatedEvent;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.spi.UserAuthorityCache;
import com.mtfm.deadman.common.spi.UserRoleAssignment;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.system.aspect.ProtectSuperAdminUser;
import com.mtfm.deadman.system.domain.department.DepartmentOperations;
import com.mtfm.deadman.system.domain.position.UserPositionOperations;
import com.mtfm.deadman.system.dto.user.CreateUserRequest;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserAccountService;
import com.mtfm.deadman.system.service.UserBaseService;
import com.mtfm.deadman.system.service.UserPasswordService;
import com.mtfm.deadman.system.service.UserService;
import com.mtfm.deadman.system.util.UserCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户领域写操作：创建、更新、删除及账号/组织/角色编排。
 */
@Service
@RequiredArgsConstructor
public class UserOperations {

    private final UserBaseService userBaseService;
    private final UserService userService;
    private final UserAccountService userAccountService;
    private final UserPasswordService userPasswordService;
    private final UserRoleAssignment userRoleAssignment;
    private final UserAuthorityCache userAuthorityCache;
    private final DeadmanProperties deadmanProperties;
    private final DepartmentOperations departmentOperations;
    private final UserPositionOperations userPositionOperations;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 新增用户并绑定默认 USER 角色。
     *
     * @param request 新增用户请求
     * @return 新建用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(CreateUserRequest request) {
        if (userAccountService.existsUsername(request.username())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }
        if (StringUtils.hasText(request.phone()) && userAccountService.existsPhone(request.phone(), null)) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        String userCode = UserCodeGenerator.generate(deadmanProperties.getUser().getUserCodePrefix());
        String nickname = StringUtils.hasText(request.nickname()) ? request.nickname() : request.username();

        UserBase userBase = UserBase.builder()
                .userCode(userCode)
                .nickname(nickname)
                .avatar(request.avatar())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        userBaseService.save(userBase);

        departmentOperations.replaceUserDepartments(
                userBase.getId(), request.departmentIds(), request.primaryDepartmentId());
        userPositionOperations.replaceUserPositionBindings(userBase.getId(), request.positionBindings());

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

        return userBase.getId();
    }

    /**
     * 更新昵称、头像、状态、手机号、部门或职位。
     *
     * @param userId  用户 ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    @ProtectSuperAdminUser(condition = "#request.status() != null && #request.status() == 0")
    public void updateUser(Long userId, UpdateUserRequest request) {
        UserBase user = userBaseService.requireById(userId);

        boolean baseChanged = false;
        boolean departmentChanged = false;
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
            baseChanged = true;
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
            baseChanged = true;
        }
        if (request.status() != null) {
            user.setStatus(request.status());
            syncAccountStatus(userId, request.status());
            baseChanged = true;
        }
        if (request.departmentIds() != null) {
            departmentOperations.replaceUserDepartments(
                    userId, request.departmentIds(), request.primaryDepartmentId());
            departmentChanged = true;
        } else if (request.primaryDepartmentId() != null) {
            departmentOperations.replaceUserDepartments(
                    userId, departmentOperations.findDepartmentIdsByUser(userId), request.primaryDepartmentId());
            departmentChanged = true;
        }
        if (request.positionBindings() != null) {
            userPositionOperations.replaceUserPositionBindings(userId, request.positionBindings());
        }
        if (request.phone() != null) {
            userAccountService.bindOrUpdatePhone(userId, request.phone());
        }

        boolean anyChanged = baseChanged || departmentChanged || request.positionBindings() != null || request.phone() != null;
        if (baseChanged) {
            userBaseService.updateById(user);
        }
        if (anyChanged) {
            userAuthorityCache.evictUserAuthorities(userId);
            evictProfileCache(user.getUserCode());
            eventPublisher.publishEvent(new UserUpdatedEvent(userId, departmentChanged));
        }
    }

    /**
     * 逻辑删除用户及全部关联数据。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @ProtectSuperAdminUser
    public void deleteUser(Long userId) {
        UserBase user = userBaseService.requireById(userId);

        userRoleAssignment.removeAllUserRoles(userId);
        departmentOperations.removeAllDepartmentsForUser(userId);
        userPositionOperations.removeByUserId(userId);
        userAccountService.removeByUserId(userId);
        userPasswordService.removeByUserId(userId);
        userBaseService.removeById(userId);

        userAuthorityCache.evictUserAuthorities(userId);
        evictProfileCache(user.getUserCode());
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }

    /**
     * 为用户分配角色（覆盖式）。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    @ProtectSuperAdminUser(condition = "@roleAssignmentGuard.wouldRemoveSuperAdmin(#userId, #roleIds)")
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        userRoleAssignment.assignUserRoles(userId, roleIds);
    }

    /**
     * 管理端重置用户密码。
     *
     * @param userId      用户 ID
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId, String newPassword) {
        UserBase user = userBaseService.requireById(userId);
        userPasswordService.resetPassword(userId, newPassword);
        evictProfileCache(user.getUserCode());
    }

    private void evictProfileCache(String userCode) {
        if (StringUtils.hasText(userCode)) {
            userService.evictProfileCache(userCode);
        }
    }

    private void syncAccountStatus(Long userId, Integer status) {
        List<UserAccount> accounts = userAccountService.listByUserId(userId);
        for (UserAccount account : accounts) {
            account.setStatus(status);
        }
        if (!accounts.isEmpty()) {
            userAccountService.updateBatchById(accounts);
        }
    }
}
