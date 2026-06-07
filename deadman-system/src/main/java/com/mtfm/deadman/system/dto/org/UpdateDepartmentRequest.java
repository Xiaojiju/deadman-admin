package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.Size;

/**
 * 更新部门请求（字段为 null 表示不修改）。
 *
 * @param parentId  上级部门 ID
 * @param deptName  部门名称
 * @param sortOrder 排序号
 * @param status    状态：0-禁用，1-启用
 */
public record UpdateDepartmentRequest(
        Long parentId, @Size(max = 128) String deptName, Integer sortOrder, Integer status) {
}
