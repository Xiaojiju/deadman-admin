package com.mtfm.deadman.system.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建字典项。不传上级时落在第 1 级。
 *
 * @param parentId 上级字典项 ID，null 表示第 1 级
 * @param itemCode 组内编码
 * @param itemLabel 显示名称
 * @param itemValue 业务取值
 * @param remark 备注
 * @param sortOrder 排序号，null 时为 0
 */
public record CreateDictItemRequest(
        Long parentId,
        @NotBlank(message = "字典项编码不能为空")
                @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "字典项编码须为大写字母、数字或下划线，且以字母开头")
                String itemCode,
        @NotBlank(message = "字典项名称不能为空") @Size(max = 128) String itemLabel,
        @Size(max = 256) String itemValue,
        @Size(max = 255) String remark,
        Integer sortOrder) {
}
