package com.mtfm.deadman.system.dto.dict;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建字典组。
 *
 * @param groupCode 字典组编码
 * @param groupName 字典组名称
 * @param maxLevel 允许的最大层数，业主按组选择 1、2 或 3
 * @param remark 备注
 * @param sortOrder 排序号，null 时为 0
 */
public record CreateDictGroupRequest(
        @NotBlank(message = "字典组编码不能为空")
                @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "字典组编码须为大写字母、数字或下划线，且以字母开头")
                String groupCode,
        @NotBlank(message = "字典组名称不能为空") @Size(max = 128) String groupName,
        @NotNull(message = "字典层数不能为空") @Min(1) @Max(3) Integer maxLevel,
        @Size(max = 255) String remark,
        Integer sortOrder) {
}
