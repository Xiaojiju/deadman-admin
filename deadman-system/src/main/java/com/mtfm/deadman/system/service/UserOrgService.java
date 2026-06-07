package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户组织归属校验与展示组装。
 */
@Service
@RequiredArgsConstructor
public class UserOrgService {

    private final SysDepartmentService sysDepartmentService;
    private final SysPositionService sysPositionService;
    private final UserBaseService userBaseService;

    /**
     * 校验并解析用户部门、职位（创建场景）。
     *
     * @param departmentId 部门 ID
     * @param positionIds  职位 ID 列表
     * @return 解析后的部门与职位 ID 列表
     */
    public OrgAssignment resolveForCreate(Long departmentId, List<Long> positionIds) {
        return resolveAssignment(departmentId, positionIds, null, null, false);
    }

    /**
     * 校验部门变更后，现有职位是否仍合法。
     *
     * @param departmentId 部门 ID
     * @param positionIds  当前职位 ID 列表
     */
    public void validatePositionsForDepartment(Long departmentId, List<Long> positionIds) {
        List<Long> normalized = DedupUtils.dedupeLongs(positionIds);
        if (departmentId != null) {
            SysDepartment department = sysDepartmentService.requireById(departmentId);
            if (department.getStatus() != null && department.getStatus() != UserStatus.ACTIVE.getValue()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "部门已禁用");
            }
        }
        for (Long positionId : normalized) {
            validatePositionForDepartment(departmentId, positionId);
        }
    }

    /**
     * 将部门 ID 转为简要引用。
     *
     * @param departmentId 部门 ID
     * @return 部门引用，不存在时返回 null
     */
    public OrgRefVO toDepartmentRef(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        SysDepartment department = sysDepartmentService.getById(departmentId);
        if (department == null) {
            return null;
        }
        return new OrgRefVO(department.getId(), department.getDeptCode(), department.getDeptName());
    }

    /**
     * 批量加载职位引用。
     *
     * @param positionIds 职位 ID 列表
     * @return 职位 ID 到引用的映射
     */
    public Map<Long, OrgRefVO> loadPositionRefs(List<Long> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = positionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysPositionService.listByIds(ids).stream()
                .collect(Collectors.toMap(
                        SysPosition::getId, p -> new OrgRefVO(p.getId(), p.getPositionCode(), p.getPositionName())));
    }

    /**
     * 批量加载部门引用。
     *
     * @param departmentIds 部门 ID 列表
     * @return 部门 ID 到引用的映射
     */
    public Map<Long, OrgRefVO> loadDepartmentRefs(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = departmentIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysDepartmentService.listByIds(ids).stream()
                .collect(Collectors.toMap(SysDepartment::getId, d -> new OrgRefVO(d.getId(), d.getDeptCode(), d.getDeptName())));
    }

    /**
     * 判断部门下是否存在用户。
     *
     * @param departmentId 部门 ID
     * @return 是否存在关联用户
     */
    public boolean hasUsersInDepartment(Long departmentId) {
        return userBaseService.count(new LambdaQueryWrapper<UserBase>().eq(UserBase::getDepartmentId, departmentId)) > 0;
    }

    private OrgAssignment resolveAssignment(
            Long departmentId,
            List<Long> positionIds,
            Long currentDepartmentId,
            List<Long> currentPositionIds,
            boolean partialUpdate) {
        Long resolvedDept = departmentId != null ? departmentId : (partialUpdate ? currentDepartmentId : null);
        List<Long> resolvedPositions = positionIds != null
                ? DedupUtils.dedupeLongs(positionIds)
                : (partialUpdate ? DedupUtils.dedupeLongs(currentPositionIds) : List.of());

        resolvedDept = inferDepartmentIfNeeded(resolvedDept, resolvedPositions);
        validatePositionsForDepartment(resolvedDept, resolvedPositions);
        return new OrgAssignment(resolvedDept, resolvedPositions);
    }

    private Long inferDepartmentIfNeeded(Long departmentId, List<Long> positionIds) {
        if (departmentId != null || positionIds.isEmpty()) {
            return departmentId;
        }
        Long inferred = null;
        for (Long positionId : positionIds) {
            SysPosition position = sysPositionService.requireById(positionId);
            if (position.getDepartmentId() == null) {
                continue;
            }
            if (inferred == null) {
                inferred = position.getDepartmentId();
            } else if (!inferred.equals(position.getDepartmentId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "职位分属不同部门，请指定用户部门");
            }
        }
        return inferred;
    }

    private void validatePositionForDepartment(Long departmentId, Long positionId) {
        SysPosition position = sysPositionService.requireById(positionId);
        if (position.getStatus() != null && position.getStatus() != UserStatus.ACTIVE.getValue()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "职位已禁用");
        }
        if (position.getDepartmentId() != null
                && departmentId != null
                && !position.getDepartmentId().equals(departmentId)) {
            throw new BusinessException(ResultCode.POSITION_DEPT_MISMATCH);
        }
    }

    /**
     * 解析后的部门与职位 ID 列表。
     *
     * @param departmentId 部门 ID
     * @param positionIds  职位 ID 列表
     */
    public record OrgAssignment(Long departmentId, List<Long> positionIds) {
    }
}
