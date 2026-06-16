package com.mtfm.deadman.system.domain.department;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.SysDepartmentService;
import com.mtfm.deadman.system.service.SysPositionService;
import com.mtfm.deadman.system.service.UserBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门领域校验：部门、用户、职位归属等业务不变量。
 */
@Component
@RequiredArgsConstructor
public class DepartmentValidator {

    private final SysDepartmentService sysDepartmentService;
    private final SysPositionService sysPositionService;
    private final UserBaseService userBaseService;

    /**
     * 校验部门存在且处于启用状态。
     *
     * @param deptId 部门 ID
     * @return 部门实体
     */
    public SysDepartment requireActiveDepartment(Long deptId) {
        SysDepartment department = sysDepartmentService.requireById(deptId);
        if (department.getStatus() != null && department.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门已禁用");
        }
        return department;
    }

    /**
     * 校验用户存在。
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    public UserBase requireUser(Long userId) {
        return userBaseService.requireById(userId);
    }

    /**
     * 批量校验用户存在。
     *
     * @param userIds 用户 ID 列表
     */
    public void requireUsersExist(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> distinctIds = userIds.stream().distinct().toList();
        long count = userBaseService.countByIds(distinctIds);
        if (count != distinctIds.size()) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
    }

    /**
     * 校验职位存在、启用，且归属指定部门（全局职位 departmentId 为 null 时不校验部门归属）。
     *
     * @param departmentId 用户绑定部门 ID
     * @param positionId     职位 ID
     * @return 职位实体
     */
    public SysPosition requirePositionForDepartment(Long departmentId, Long positionId) {
        SysPosition position = sysPositionService.requireById(positionId);
        if (position.getStatus() != null && position.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "职位已禁用");
        }
        if (position.getDepartmentId() != null
                && departmentId != null
                && !position.getDepartmentId().equals(departmentId)) {
            throw new BusinessException(ResultCode.POSITION_DEPT_MISMATCH);
        }
        return position;
    }
}
