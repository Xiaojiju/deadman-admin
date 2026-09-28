package com.mtfm.deadman.system.vo.dict;

import java.time.LocalDateTime;

/**
 * 字典项详情。
 *
 * @param id 字典项主键
 * @param groupId 字典组主键
 * @param parentId 上级字典项 ID
 * @param itemCode 组内编码
 * @param itemLabel 显示名称
 * @param itemValue 业务取值
 * @param itemLevel 当前层级
 * @param remark 备注
 * @param sortOrder 排序号
 * @param status 状态
 * @param createTime 创建时间
 * @param updateTime 更新时间
 */
public record DictItemVO(
        Long id,
        Long groupId,
        Long parentId,
        String itemCode,
        String itemLabel,
        String itemValue,
        Integer itemLevel,
        String remark,
        Integer sortOrder,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
