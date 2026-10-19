package com.mtfm.deadman.system.vo.dict;

import java.util.List;

/**
 * 字典项树节点。
 *
 * @param id 字典项主键
 * @param parentId 上级字典项 ID
 * @param itemCode 组内编码
 * @param itemLabel 显示名称
 * @param itemValue 业务取值
 * @param itemLevel 当前层级
 * @param sortOrder 排序号
 * @param status 状态
 * @param children 下级字典项
 */
public record DictItemTreeVO(
        Long id,
        Long parentId,
        String itemCode,
        String itemLabel,
        String itemValue,
        Integer itemLevel,
        Integer sortOrder,
        Integer status,
        List<DictItemTreeVO> children) {
}
