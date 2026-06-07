package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建职位请求。
 *
 * @param departmentId 所属部门 ID，null 表示全局职位
 * @param positionCode 职位编码
 * @param positionName 职位名称
 * @param sortOrder    排序号，null 时默认为 0
 */
public record CreatePositionRequest(
        Long departmentId,
        @NotBlank(message = "职位编码不能为空")
                @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "职位编码须为大写字母、数字或下划线，且以字母开头")
                String positionCode,
        @NotBlank(message = "职位名称不能为空") @Size(max = 128) String positionName,
        Integer sortOrder) {
}
