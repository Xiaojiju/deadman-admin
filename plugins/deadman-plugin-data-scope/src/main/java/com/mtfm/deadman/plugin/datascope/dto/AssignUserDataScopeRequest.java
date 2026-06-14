package com.mtfm.deadman.plugin.datascope.dto;

import com.mtfm.deadman.plugin.datascope.model.DataScopeType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 分配用户数据范围请求（与角色独立配置）。
 *
 * @param scopeType     数据范围类型
 * @param customDeptIds CUSTOM 范围下的可见部门 ID 列表
 */
public record AssignUserDataScopeRequest(
        @NotNull(message = "数据范围类型不能为空") DataScopeType scopeType, List<Long> customDeptIds) {
}
