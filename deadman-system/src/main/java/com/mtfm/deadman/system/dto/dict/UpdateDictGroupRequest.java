package com.mtfm.deadman.system.dto.dict;

import com.mtfm.deadman.common.validation.UserStatusValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 更新字典组。字段为 null 表示不修改。缩小层数时，已有字典项不能深于新上限。
 *
 * @param groupName 字典组名称
 * @param maxLevel 允许的最大层数，1、2 或 3
 * @param remark 备注
 * @param sortOrder 排序号
 * @param status 状态：0-禁用，1-启用
 */
public record UpdateDictGroupRequest(
        @Size(max = 128) String groupName,
        @Min(1) @Max(3) Integer maxLevel,
        @Size(max = 255) String remark,
        Integer sortOrder,
        @UserStatusValue Integer status) {
}
