package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.NotNull;

/**
 * 用户在部门下的职位绑定请求。
 *
 * @param departmentId 部门 ID，用户须已绑定该部门
 * @param positionId   职位 ID，须归属该部门或为全局职位
 */
public record UserPositionBindingRequest(@NotNull(message = "部门 ID 不能为空") Long departmentId,
        @NotNull(message = "职位 ID 不能为空") Long positionId) {
}
