package com.mtfm.deadman.system.vo.dict;

import java.util.List;

/**
 * 客户端可见的字典组。只包含启用的字典项。
 *
 * @param groupCode 字典组编码
 * @param groupName 字典组名称
 * @param maxLevel 该组允许的最大层数
 * @param items 启用字典项树
 */
public record ClientDictVO(
        String groupCode,
        String groupName,
        Integer maxLevel,
        List<DictItemTreeVO> items) {
}
