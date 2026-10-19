package com.mtfm.deadman.system.domain.dict;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mtfm.deadman.system.support.SystemErrorCodes;
import com.mtfm.deadman.system.support.SystemMessages;

/**
 * 字典层数规则。全局最多 3 级，每个字典组可单独收成 1 级或 2 级。
 */
public final class DictDepth {

    /** 最少层数 */
    public static final int MIN_LEVEL = 1;

    /** 最多层数 */
    public static final int MAX_LEVEL = 3;

    private DictDepth() {
    }

    /**
     * 校验字典组声明的层数。
     *
     * @param maxLevel 允许的最大层数
     */
    public static void requireGroupMaxLevel(int maxLevel) {
        if (maxLevel < MIN_LEVEL || maxLevel > MAX_LEVEL) {
            throw SystemMessages.of(SystemErrorCodes.DICT_LEVEL_INVALID);
        }
    }

    /**
     * 由上级层级得到当前项层级。无上级时为第 1 级。
     *
     * @param parentLevel 上级层级，根节点传 null
     * @param groupMaxLevel 字典组允许的最大层数
     * @return 当前层级
     */
    public static int levelOf(Integer parentLevel, int groupMaxLevel) {
        int level = parentLevel == null ? MIN_LEVEL : parentLevel + 1;
        if (level > MAX_LEVEL || level > groupMaxLevel) {
            throw SystemMessages.of(SystemErrorCodes.DICT_LEVEL_EXCEEDED);
        }
        return level;
    }

    /**
     * 计算节点自身加上全部下级所占的层数。孤立节点为 1。
     *
     * @param itemId 字典项 ID
     * @param childIds 上级 ID 到直接下级 ID 的映射
     * @return 子树跨度
     */
    public static int subtreeSpan(Long itemId, Map<Long, List<Long>> childIds) {
        int span = 1;
        for (Long childId : childIds.getOrDefault(itemId, List.of())) {
            span = Math.max(span, 1 + subtreeSpan(childId, childIds));
        }
        return span;
    }

    /**
     * 移动后的最深层不能超过字典组上限。
     *
     * @param newLevel 移动后的本节点层级
     * @param span 子树跨度
     * @param groupMaxLevel 字典组允许的最大层数
     */
    public static void requireMoveFits(int newLevel, int span, int groupMaxLevel) {
        if (newLevel + span - 1 > groupMaxLevel) {
            throw SystemMessages.of(SystemErrorCodes.DICT_LEVEL_EXCEEDED);
        }
    }

    /**
     * 组装下级索引，便于计算子树跨度。
     *
     * @param parentIds 节点 ID 与其上级 ID
     * @return 上级到直接下级的映射
     */
    public static Map<Long, List<Long>> childrenIndex(Map<Long, Long> parentIds) {
        Map<Long, List<Long>> children = new HashMap<>();
        parentIds.forEach((id, parentId) -> {
            if (parentId != null) {
                children.computeIfAbsent(parentId, ignored -> new java.util.ArrayList<>()).add(id);
            }
        });
        return children;
    }
}
