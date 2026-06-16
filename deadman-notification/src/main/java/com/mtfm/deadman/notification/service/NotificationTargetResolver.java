package com.mtfm.deadman.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.notification.dto.SendNotificationRequest;
import com.mtfm.deadman.notification.enums.NotificationTargetType;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.entity.SysUserDepartment;
import com.mtfm.deadman.system.entity.SysUserPosition;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.mapper.SysUserDepartmentMapper;
import com.mtfm.deadman.system.mapper.SysUserPositionMapper;
import com.mtfm.deadman.system.service.SysDepartmentService;
import com.mtfm.deadman.system.service.SysPositionService;
import com.mtfm.deadman.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 将发送目标解析为具体用户 ID 集合。
 */
@Component
@RequiredArgsConstructor
public class NotificationTargetResolver {

    private final UserService userService;
    private final SysDepartmentService sysDepartmentService;
    private final SysPositionService sysPositionService;
    private final SysUserDepartmentMapper sysUserDepartmentMapper;
    private final SysUserPositionMapper sysUserPositionMapper;

    /**
     * 解析发送目标对应的活跃用户 ID。
     *
     * @param request 发送请求
     * @return 去重后的用户 ID
     */
    public Set<Long> resolveUserIds(SendNotificationRequest request) {
        NotificationTargetType targetType = NotificationTargetType.fromValue(request.targetType());
        return switch (targetType) {
            case USER -> resolveByUsers(request.userIds());
            case DEPARTMENT -> resolveByDepartments(request.departmentIds());
            case POSITION -> resolveByPositions(request.positionIds());
            case ALL -> resolveAllActiveUsers();
        };
    }

    private Set<Long> resolveByUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "定向发送需指定用户");
        }
        Set<Long> result = new LinkedHashSet<>();
        for (Long userId : userIds) {
            UserBase user = userService.getById(userId);
            if (user != null && isActive(user)) {
                result.add(userId);
            }
        }
        if (result.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "未找到有效的目标用户");
        }
        return result;
    }

    private Set<Long> resolveByDepartments(List<Long> departmentIds) {
        if (CollectionUtils.isEmpty(departmentIds)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "按部门发送需指定部门");
        }
        for (Long departmentId : departmentIds) {
            SysDepartment department = sysDepartmentService.getById(departmentId);
            if (department == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "部门不存在: " + departmentId);
            }
        }
        List<SysUserDepartment> bindings = sysUserDepartmentMapper.selectList(new LambdaQueryWrapper<SysUserDepartment>()
                .in(SysUserDepartment::getDeptId, departmentIds));
        Set<Long> userIds = new LinkedHashSet<>();
        for (SysUserDepartment binding : bindings) {
            UserBase user = userService.getById(binding.getUserId());
            if (user != null && isActive(user)) {
                userIds.add(binding.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门下无有效用户");
        }
        return userIds;
    }

    private Set<Long> resolveByPositions(List<Long> positionIds) {
        if (CollectionUtils.isEmpty(positionIds)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "按职位发送需指定职位");
        }
        for (Long positionId : positionIds) {
            SysPosition position = sysPositionService.getById(positionId);
            if (position == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "职位不存在: " + positionId);
            }
        }
        List<SysUserPosition> bindings = sysUserPositionMapper.selectList(
                new LambdaQueryWrapper<SysUserPosition>().in(SysUserPosition::getPositionId, positionIds));
        Set<Long> userIds = new LinkedHashSet<>();
        for (SysUserPosition binding : bindings) {
            UserBase user = userService.getById(binding.getUserId());
            if (user != null && isActive(user)) {
                userIds.add(user.getId());
            }
        }
        if (userIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "职位下无有效用户");
        }
        return userIds;
    }

    private Set<Long> resolveAllActiveUsers() {
        List<UserBase> users = userService.lambdaQuery()
                .eq(UserBase::getStatus, UserStatus.ACTIVE.getValue())
                .list();
        Set<Long> result = toActiveUserIdSet(users);
        if (result.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前无有效用户可发送");
        }
        return result;
    }

    private Set<Long> toActiveUserIdSet(List<UserBase> users) {
        Set<Long> result = new LinkedHashSet<>();
        for (UserBase user : users) {
            if (isActive(user)) {
                result.add(user.getId());
            }
        }
        return result;
    }

    private boolean isActive(UserBase user) {
        return user.getStatus() != null && user.getStatus() == UserStatus.ACTIVE.getValue();
    }
}
