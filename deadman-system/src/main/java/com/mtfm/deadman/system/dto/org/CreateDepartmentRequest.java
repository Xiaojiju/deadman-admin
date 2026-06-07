package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建部门请求。
 *
 * @param parentId  上级部门 ID，null 表示根部门
 * @param deptCode  部门编码
 * @param deptName  部门名称
 * @param sortOrder 排序号，null 时默认为 0
 */
public record CreateDepartmentRequest(
        Long parentId,
        @NotBlank(message = "部门编码不能为空")
                @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "部门编码须为大写字母、数字或下划线，且以字母开头")
                String deptCode,
        @NotBlank(message = "部门名称不能为空") @Size(max = 128) String deptName,
        Integer sortOrder) {
}
