package com.mtfm.deadman.system.dto.dict;

import com.mtfm.deadman.common.validation.UserStatusValue;
import jakarta.validation.constraints.Size;

/**
 * 更新字典项。层级调整受字典组最大层数约束。
 *
 * @param moveToRoot 为 true 时改为第 1 级
 * @param parentId 新的上级字典项 ID；moveToRoot 不为 true 且本字段为 null 时不调整上级
 * @param itemLabel 显示名称
 * @param itemValue 业务取值
 * @param remark 备注
 * @param sortOrder 排序号
 * @param status 状态：0-禁用，1-启用
 */
public record UpdateDictItemRequest(
        Boolean moveToRoot,
        Long parentId,
        @Size(max = 128) String itemLabel,
        @Size(max = 256) String itemValue,
        @Size(max = 255) String remark,
        Integer sortOrder,
        @UserStatusValue Integer status) {
}
