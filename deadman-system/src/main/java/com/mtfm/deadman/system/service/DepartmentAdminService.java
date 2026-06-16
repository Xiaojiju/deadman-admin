package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.domain.department.DepartmentOperations;
import com.mtfm.deadman.system.dto.org.CreateDepartmentRequest;
import com.mtfm.deadman.system.dto.org.UpdateDepartmentRequest;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.vo.org.DepartmentTreeVO;
import com.mtfm.deadman.system.vo.org.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门管理。
 */
@Service
@RequiredArgsConstructor
public class DepartmentAdminService {

    private final SysDepartmentService sysDepartmentService;
    private final DepartmentOperations departmentOperations;

    /**
     * 查询全部部门（扁平列表）。
     *
     * @return 部门列表
     */
    public List<DepartmentVO> listDepartments() {
        return sysDepartmentService.list(new LambdaQueryWrapper<SysDepartment>()
                        .orderByAsc(SysDepartment::getSortOrder)
                        .orderByAsc(SysDepartment::getDeptCode))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 查询部门树。
     *
     * @return 根部门及下级嵌套树
     */
    public List<DepartmentTreeVO> listDepartmentTree() {
        List<SysDepartment> all = sysDepartmentService.list(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSortOrder)
                .orderByAsc(SysDepartment::getDeptCode));
        Map<Long, List<SysDepartment>> childrenMap =
                all.stream().collect(Collectors.groupingBy(d -> d.getParentId() == null ? 0L : d.getParentId()));
        return buildTree(childrenMap, 0L);
    }

    /**
     * 查询部门详情。
     *
     * @param departmentId 部门 ID
     * @return 部门详情
     */
    public DepartmentVO getDepartment(Long departmentId) {
        return toVO(sysDepartmentService.requireById(departmentId));
    }

    /**
     * 创建部门。
     *
     * @param request 创建请求
     * @return 新建部门详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO createDepartment(CreateDepartmentRequest request) {
        if (sysDepartmentService.count(new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getDeptCode, request.deptCode()))
                > 0) {
            throw new BusinessException(ResultCode.DEPARTMENT_CODE_EXISTS);
        }
        if (request.parentId() != null) {
            sysDepartmentService.requireById(request.parentId());
        }

        SysDepartment department = SysDepartment.builder()
                .parentId(request.parentId())
                .deptCode(request.deptCode())
                .deptName(request.deptName())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        sysDepartmentService.save(department);
        return toVO(department);
    }

    /**
     * 更新部门。
     *
     * @param departmentId 部门 ID
     * @param request      更新请求
     * @return 更新后的部门详情
     */
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO updateDepartment(Long departmentId, UpdateDepartmentRequest request) {
        SysDepartment department = sysDepartmentService.requireById(departmentId);

        if (request.parentId() != null) {
            if (request.parentId().equals(departmentId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "上级部门不能为自身");
            }
            sysDepartmentService.requireById(request.parentId());
            assertNoCycle(departmentId, request.parentId());
            department.setParentId(request.parentId());
        }
        if (request.deptName() != null) {
            department.setDeptName(request.deptName());
        }
        if (request.sortOrder() != null) {
            department.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            department.setStatus(request.status());
        }

        sysDepartmentService.updateById(department);
        return toVO(department);
    }

    /**
     * 删除部门（存在子部门或关联用户时不允许）。
     *
     * @param departmentId 部门 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long departmentId) {
        sysDepartmentService.requireById(departmentId);
        long childCount = sysDepartmentService.count(
                new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getParentId, departmentId));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.DEPARTMENT_HAS_CHILDREN);
        }
        if (departmentOperations.hasUsersInDepartment(departmentId)) {
            throw new BusinessException(ResultCode.DEPARTMENT_HAS_USERS);
        }
        sysDepartmentService.removeById(departmentId);
    }

    private List<DepartmentTreeVO> buildTree(Map<Long, List<SysDepartment>> childrenMap, Long parentKey) {
        List<SysDepartment> children = childrenMap.getOrDefault(parentKey, List.of());
        return children.stream()
                .sorted(Comparator.comparing(SysDepartment::getSortOrder).thenComparing(SysDepartment::getDeptCode))
                .map(dept -> new DepartmentTreeVO(
                        dept.getId(),
                        dept.getParentId(),
                        dept.getDeptCode(),
                        dept.getDeptName(),
                        dept.getSortOrder(),
                        dept.getStatus(),
                        buildTree(childrenMap, dept.getId())))
                .toList();
    }

    private void assertNoCycle(Long departmentId, Long newParentId) {
        Long cursor = newParentId;
        List<Long> visited = new ArrayList<>();
        while (cursor != null) {
            if (cursor.equals(departmentId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "上级部门不能形成环");
            }
            if (visited.contains(cursor)) {
                break;
            }
            visited.add(cursor);
            SysDepartment parent = sysDepartmentService.getById(cursor);
            cursor = parent == null ? null : parent.getParentId();
        }
    }

    private DepartmentVO toVO(SysDepartment department) {
        return new DepartmentVO(
                department.getId(),
                department.getParentId(),
                department.getDeptCode(),
                department.getDeptName(),
                department.getSortOrder(),
                department.getStatus(),
                department.getCreateTime(),
                department.getUpdateTime());
    }
}
