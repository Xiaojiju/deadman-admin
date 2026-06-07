package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.Size;

/**
 * 更新职位请求（字段为 null 表示不修改）。
 *
 * @param departmentId 所属部门 ID
 * @param positionName 职位名称
 * @param sortOrder    排序号
 * @param status       状态：0-禁用，1-启用
 */
public record UpdatePositionRequest(
        Long departmentId, @Size(max = 128) String positionName, Integer sortOrder, Integer status) {
}
