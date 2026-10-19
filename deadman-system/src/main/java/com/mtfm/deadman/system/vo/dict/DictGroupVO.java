package com.mtfm.deadman.system.vo.dict;

import java.time.LocalDateTime;

/**
 * 字典组详情。
 *
 * @param id 字典组主键
 * @param groupCode 字典组编码
 * @param groupName 字典组名称
 * @param maxLevel 允许的最大层数
 * @param remark 备注
 * @param sortOrder 排序号
 * @param status 状态
 * @param createTime 创建时间
 * @param updateTime 更新时间
 */
public record DictGroupVO(
        Long id,
        String groupCode,
        String groupName,
        Integer maxLevel,
        String remark,
        Integer sortOrder,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
