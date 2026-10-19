package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.system.domain.dict.DictDepth;
import com.mtfm.deadman.system.dto.dict.CreateDictGroupRequest;
import com.mtfm.deadman.system.dto.dict.CreateDictItemRequest;
import com.mtfm.deadman.system.dto.dict.UpdateDictGroupRequest;
import com.mtfm.deadman.system.dto.dict.UpdateDictItemRequest;
import com.mtfm.deadman.system.entity.SysDictGroup;
import com.mtfm.deadman.system.entity.SysDictItem;
import com.mtfm.deadman.system.vo.dict.ClientDictVO;
import com.mtfm.deadman.system.vo.dict.DictGroupVO;
import com.mtfm.deadman.system.vo.dict.DictItemTreeVO;
import com.mtfm.deadman.system.vo.dict.DictItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mtfm.deadman.system.support.SystemErrorCodes;
import com.mtfm.deadman.system.support.SystemMessages;

/**
 * 字典组与字典项管理。层数由字典组的 maxLevel 决定，且不超过 3。
 */
@Service
@RequiredArgsConstructor
public class DictAdminService {

    private final SysDictGroupService sysDictGroupService;
    private final SysDictItemService sysDictItemService;

    /**
     * 字典组列表。
     *
     * @return 按排序号、编码升序
     */
    public List<DictGroupVO> listGroups() {
        return sysDictGroupService
                .list(new LambdaQueryWrapper<SysDictGroup>()
                        .orderByAsc(SysDictGroup::getSortOrder)
                        .orderByAsc(SysDictGroup::getGroupCode))
                .stream()
                .map(this::toGroupVO)
                .toList();
    }

    /**
     * 字典组详情。
     *
     * @param groupId 字典组 ID
     * @return 字典组
     */
    public DictGroupVO getGroup(Long groupId) {
        return toGroupVO(sysDictGroupService.requireById(groupId));
    }

    /**
     * 按编码读取字典组。
     *
     * @param groupCode 字典组编码
     * @return 字典组
     */
    public DictGroupVO getGroupByCode(String groupCode) {
        return toGroupVO(sysDictGroupService.requireByCode(groupCode));
    }

    /**
     * 创建字典组。
     *
     * @param request 创建请求
     * @return 新建字典组
     */
    @Transactional(rollbackFor = Exception.class)
    public DictGroupVO createGroup(CreateDictGroupRequest request) {
        DictDepth.requireGroupMaxLevel(request.maxLevel());
        if (sysDictGroupService.count(
                        new LambdaQueryWrapper<SysDictGroup>().eq(SysDictGroup::getGroupCode, request.groupCode()))
                > 0) {
            throw SystemMessages.of(SystemErrorCodes.DICT_GROUP_CODE_EXISTS);
        }
        SysDictGroup group = SysDictGroup.builder()
                .groupCode(request.groupCode())
                .groupName(request.groupName())
                .maxLevel(request.maxLevel())
                .remark(request.remark())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        sysDictGroupService.save(group);
        return toGroupVO(group);
    }

    /**
     * 更新字典组。缩小层数时，已有字典项不能深于新上限。
     *
     * @param groupId 字典组 ID
     * @param request 更新请求
     * @return 更新后的字典组
     */
    @Transactional(rollbackFor = Exception.class)
    public DictGroupVO updateGroup(Long groupId, UpdateDictGroupRequest request) {
        SysDictGroup group = sysDictGroupService.requireById(groupId);
        if (request.maxLevel() != null && !request.maxLevel().equals(group.getMaxLevel())) {
            DictDepth.requireGroupMaxLevel(request.maxLevel());
            int deepest = loadItems(groupId, false).stream()
                    .map(SysDictItem::getItemLevel)
                    .filter(level -> level != null)
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
            if (deepest > request.maxLevel()) {
                throw SystemMessages.of(SystemErrorCodes.DICT_LEVEL_SHRINK_BLOCKED);
            }
            group.setMaxLevel(request.maxLevel());
        }
        if (request.groupName() != null) {
            group.setGroupName(request.groupName());
        }
        if (request.remark() != null) {
            group.setRemark(request.remark());
        }
        if (request.sortOrder() != null) {
            group.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            group.setStatus(request.status());
        }
        sysDictGroupService.updateById(group);
        return toGroupVO(group);
    }

    /**
     * 删除字典组。组内仍有字典项时拒绝。
     *
     * @param groupId 字典组 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        sysDictGroupService.requireById(groupId);
        if (sysDictItemService.count(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getGroupId, groupId)) > 0) {
            throw SystemMessages.of(SystemErrorCodes.DICT_GROUP_HAS_ITEMS);
        }
        sysDictGroupService.removeById(groupId);
    }

    /**
     * 组内字典项扁平列表。
     *
     * @param groupId 字典组 ID
     * @return 字典项
     */
    public List<DictItemVO> listItems(Long groupId) {
        sysDictGroupService.requireById(groupId);
        return loadItems(groupId, false).stream().map(this::toItemVO).toList();
    }

    /**
     * 组内字典树。
     *
     * @param groupId 字典组 ID
     * @param enabledOnly 为 true 时只保留启用项，禁用项的下级也不出现
     * @return 第 1 级节点及其下级
     */
    public List<DictItemTreeVO> listTree(Long groupId, boolean enabledOnly) {
        sysDictGroupService.requireById(groupId);
        return buildTree(loadItems(groupId, enabledOnly));
    }

    /**
     * 按编码读取字典树。
     *
     * @param groupCode 字典组编码
     * @param enabledOnly 为 true 时只保留启用项
     * @return 字典树
     */
    public List<DictItemTreeVO> listTreeByCode(String groupCode, boolean enabledOnly) {
        SysDictGroup group = sysDictGroupService.requireByCode(groupCode);
        return buildTree(loadItems(group.getId(), enabledOnly));
    }

    /**
     * 客户端按组编码读取已启用字典。未传编码时返回全部启用组，只包含启用的字典项。
     *
     * @param groupCodes 字典组编码，可多个；为空则不过滤
     * @return 字典组及启用项树
     */
    public List<ClientDictVO> listEnabled(List<String> groupCodes) {
        List<String> codes = groupCodes == null ? List.of() : groupCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        List<SysDictGroup> groups;
        if (codes.isEmpty()) {
            groups = sysDictGroupService.list(new LambdaQueryWrapper<SysDictGroup>()
                    .eq(SysDictGroup::getStatus, UserStatus.ACTIVE.getValue())
                    .orderByAsc(SysDictGroup::getSortOrder)
                    .orderByAsc(SysDictGroup::getGroupCode));
        } else {
            groups = new ArrayList<>();
            for (String code : codes) {
                SysDictGroup group = sysDictGroupService.requireByCode(code);
                if (group.getStatus() == null || group.getStatus() != UserStatus.ACTIVE.getValue()) {
                    throw SystemMessages.of(SystemErrorCodes.DICT_GROUP_NOT_FOUND);
                }
                groups.add(group);
            }
        }
        return groups.stream()
                .map(group -> new ClientDictVO(
                        group.getGroupCode(),
                        group.getGroupName(),
                        group.getMaxLevel(),
                        buildTree(loadItems(group.getId(), true))))
                .toList();
    }

    /**
     * 创建字典项。层级为上级加 1，且不能超过字典组声明的层数。
     *
     * @param groupId 字典组 ID
     * @param request 创建请求
     * @return 新建字典项
     */
    @Transactional(rollbackFor = Exception.class)
    public DictItemVO createItem(Long groupId, CreateDictItemRequest request) {
        SysDictGroup group = sysDictGroupService.requireById(groupId);
        if (sysDictItemService.count(new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getGroupId, groupId)
                        .eq(SysDictItem::getItemCode, request.itemCode()))
                > 0) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_CODE_EXISTS);
        }
        Integer parentLevel = null;
        if (request.parentId() != null) {
            parentLevel = requireParent(groupId, request.parentId()).getItemLevel();
        }
        int level = DictDepth.levelOf(parentLevel, group.getMaxLevel());
        SysDictItem item = SysDictItem.builder()
                .groupId(groupId)
                .parentId(request.parentId())
                .itemCode(request.itemCode())
                .itemLabel(request.itemLabel())
                .itemValue(request.itemValue())
                .itemLevel(level)
                .remark(request.remark())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
                .status(UserStatus.ACTIVE.getValue())
                .build();
        sysDictItemService.save(item);
        return toItemVO(item);
    }

    /**
     * 更新字典项。调整上级时整棵子树一起改层级。
     *
     * @param groupId 字典组 ID
     * @param itemId 字典项 ID
     * @param request 更新请求
     * @return 更新后的字典项
     */
    @Transactional(rollbackFor = Exception.class)
    public DictItemVO updateItem(Long groupId, Long itemId, UpdateDictItemRequest request) {
        SysDictGroup group = sysDictGroupService.requireById(groupId);
        SysDictItem item = requireItemInGroup(groupId, itemId);
        if (Boolean.TRUE.equals(request.moveToRoot()) || request.parentId() != null) {
            Long newParentId = Boolean.TRUE.equals(request.moveToRoot()) ? null : request.parentId();
            moveItem(group, item, newParentId);
        }
        if (request.itemLabel() != null) {
            item.setItemLabel(request.itemLabel());
        }
        if (request.itemValue() != null) {
            item.setItemValue(request.itemValue());
        }
        if (request.remark() != null) {
            item.setRemark(request.remark());
        }
        if (request.sortOrder() != null) {
            item.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            item.setStatus(request.status());
        }
        sysDictItemService.updateById(item);
        return toItemVO(item);
    }

    /**
     * 删除字典项。仍有下级时拒绝。
     *
     * @param groupId 字典组 ID
     * @param itemId 字典项 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long groupId, Long itemId) {
        requireItemInGroup(groupId, itemId);
        if (sysDictItemService.count(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getParentId, itemId)) > 0) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_HAS_CHILDREN);
        }
        sysDictItemService.removeById(itemId);
    }

    private void moveItem(SysDictGroup group, SysDictItem item, Long newParentId) {
        if (newParentId != null && newParentId.equals(item.getId())) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_PARENT_INVALID);
        }
        List<SysDictItem> items = loadItems(group.getId(), false);
        Map<Long, SysDictItem> byId = new HashMap<>();
        Map<Long, Long> parentIds = new HashMap<>();
        for (SysDictItem current : items) {
            byId.put(current.getId(), current);
            parentIds.put(current.getId(), current.getParentId());
        }
        if (newParentId != null && !byId.containsKey(newParentId)) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_PARENT_INVALID);
        }
        if (isDescendant(item.getId(), newParentId, parentIds)) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_PARENT_INVALID);
        }
        Integer parentLevel = newParentId == null ? null : byId.get(newParentId).getItemLevel();
        int newLevel = DictDepth.levelOf(parentLevel, group.getMaxLevel());
        int span = DictDepth.subtreeSpan(item.getId(), DictDepth.childrenIndex(parentIds));
        DictDepth.requireMoveFits(newLevel, span, group.getMaxLevel());
        item.setParentId(newParentId);
        item.setItemLevel(newLevel);
        rewriteDescendantLevels(item.getId(), newLevel, DictDepth.childrenIndex(parentIds), byId);
    }

    private void rewriteDescendantLevels(
            Long itemId, int level, Map<Long, List<Long>> children, Map<Long, SysDictItem> byId) {
        for (Long childId : children.getOrDefault(itemId, List.of())) {
            SysDictItem child = byId.get(childId);
            child.setItemLevel(level + 1);
            sysDictItemService.updateById(child);
            rewriteDescendantLevels(childId, level + 1, children, byId);
        }
    }

    private boolean isDescendant(Long itemId, Long candidateParentId, Map<Long, Long> parentIds) {
        Long cursor = candidateParentId;
        while (cursor != null) {
            if (cursor.equals(itemId)) {
                return true;
            }
            cursor = parentIds.get(cursor);
        }
        return false;
    }

    private SysDictItem requireParent(Long groupId, Long parentId) {
        SysDictItem parent = sysDictItemService.requireById(parentId);
        if (!groupId.equals(parent.getGroupId())) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_PARENT_INVALID);
        }
        return parent;
    }

    private SysDictItem requireItemInGroup(Long groupId, Long itemId) {
        SysDictItem item = sysDictItemService.requireById(itemId);
        if (!groupId.equals(item.getGroupId())) {
            throw SystemMessages.of(SystemErrorCodes.DICT_ITEM_NOT_FOUND);
        }
        return item;
    }

    private List<SysDictItem> loadItems(Long groupId, boolean enabledOnly) {
        List<SysDictItem> items = sysDictItemService.list(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getGroupId, groupId)
                .orderByAsc(SysDictItem::getSortOrder)
                .orderByAsc(SysDictItem::getItemCode));
        if (!enabledOnly) {
            return items;
        }
        return items.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == UserStatus.ACTIVE.getValue())
                .toList();
    }

    private List<DictItemTreeVO> buildTree(List<SysDictItem> items) {
        Map<Long, List<SysDictItem>> children = new HashMap<>();
        for (SysDictItem item : items) {
            Long parentKey = item.getParentId() == null ? 0L : item.getParentId();
            children.computeIfAbsent(parentKey, ignored -> new ArrayList<>()).add(item);
        }
        return toTree(children, 0L);
    }

    private List<DictItemTreeVO> toTree(Map<Long, List<SysDictItem>> children, Long parentKey) {
        return children.getOrDefault(parentKey, List.of()).stream()
                .sorted(Comparator.comparing(SysDictItem::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysDictItem::getItemCode))
                .map(item -> new DictItemTreeVO(
                        item.getId(),
                        item.getParentId(),
                        item.getItemCode(),
                        item.getItemLabel(),
                        item.getItemValue(),
                        item.getItemLevel(),
                        item.getSortOrder(),
                        item.getStatus(),
                        toTree(children, item.getId())))
                .toList();
    }

    private DictGroupVO toGroupVO(SysDictGroup group) {
        return new DictGroupVO(
                group.getId(),
                group.getGroupCode(),
                group.getGroupName(),
                group.getMaxLevel(),
                group.getRemark(),
                group.getSortOrder(),
                group.getStatus(),
                group.getCreateTime(),
                group.getUpdateTime());
    }

    private DictItemVO toItemVO(SysDictItem item) {
        return new DictItemVO(
                item.getId(),
                item.getGroupId(),
                item.getParentId(),
                item.getItemCode(),
                item.getItemLabel(),
                item.getItemValue(),
                item.getItemLevel(),
                item.getRemark(),
                item.getSortOrder(),
                item.getStatus(),
                item.getCreateTime(),
                item.getUpdateTime());
    }
}
