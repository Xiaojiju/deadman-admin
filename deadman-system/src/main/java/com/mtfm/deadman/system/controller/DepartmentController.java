package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.dto.org.CreateDepartmentRequest;
import com.mtfm.deadman.system.dto.org.UpdateDepartmentRequest;
import com.mtfm.deadman.system.service.DepartmentAdminService;
import com.mtfm.deadman.system.vo.org.DepartmentTreeVO;
import com.mtfm.deadman.system.vo.org.DepartmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组织部门管理接口。
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentAdminService departmentAdminService;

    /**
     * 部门扁平列表
     *
     * @return 按排序号、编码升序的部门列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('dept:list:read')")
    public Result<List<DepartmentVO>> list() {
        return Result.ok(departmentAdminService.listDepartments());
    }

    /**
     * 部门树
     *
     * @return 根部门及其下级嵌套树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('dept:list:read')")
    public Result<List<DepartmentTreeVO>> tree() {
        return Result.ok(departmentAdminService.listDepartmentTree());
    }

    /**
     * 部门详情
     *
     * @param departmentId 部门 ID
     * @return 部门详情
     */
    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('dept:list:read')")
    public Result<DepartmentVO> detail(@PathVariable Long departmentId) {
        return Result.ok(departmentAdminService.getDepartment(departmentId));
    }

    /**
     * 创建部门
     *
     * @param request 创建部门请求
     * @return 新建部门详情
     */
    @PostMapping
    @PreAuthorize("hasAuthority('dept:create')")
    public Result<DepartmentVO> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return Result.ok(departmentAdminService.createDepartment(request));
    }

    /**
     * 更新部门
     *
     * @param departmentId 部门 ID
     * @param request      更新部门请求（null 字段表示不修改）
     * @return 更新后的部门详情
     */
    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<DepartmentVO> update(
            @PathVariable Long departmentId, @Valid @RequestBody UpdateDepartmentRequest request) {
        return Result.ok(departmentAdminService.updateDepartment(departmentId, request));
    }

    /**
     * 删除部门
     *
     * @param departmentId 部门 ID
     * @return 空响应
     */
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('dept:delete')")
    public Result<Void> delete(@PathVariable Long departmentId) {
        departmentAdminService.deleteDepartment(departmentId);
        return Result.ok();
    }
}
