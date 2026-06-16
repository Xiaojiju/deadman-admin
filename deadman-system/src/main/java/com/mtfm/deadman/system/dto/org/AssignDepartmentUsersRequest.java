package com.mtfm.deadman.system.dto.org;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 部门覆盖式绑定用户请求。
 *
 * @param userIds 用户 ID 列表，覆盖该部门现有成员
 */
public record AssignDepartmentUsersRequest(@NotNull(message = "用户 ID 列表不能为 null") List<Long> userIds) {
}
