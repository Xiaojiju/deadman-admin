package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.dto.dict.CreateDictGroupRequest;
import com.mtfm.deadman.system.dto.dict.CreateDictItemRequest;
import com.mtfm.deadman.system.dto.dict.UpdateDictGroupRequest;
import com.mtfm.deadman.system.dto.dict.UpdateDictItemRequest;
import com.mtfm.deadman.system.permission.SystemPermissions;
import com.mtfm.deadman.system.service.DictAdminService;
import com.mtfm.deadman.system.vo.dict.DictGroupVO;
import com.mtfm.deadman.system.vo.dict.DictItemTreeVO;
import com.mtfm.deadman.system.vo.dict.DictItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典。每个字典组自行选择 1 到 3 级，字典项不能超过该组的层数。
 */
@RestController
@RequestMapping("/api/dict-groups")
@RequiredArgsConstructor
public class DictController {

    private final DictAdminService dictAdminService;

    /**
     * 字典组列表。
     *
     * @return 字典组
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).LIST_READ)")
    public Result<List<DictGroupVO>> listGroups() {
        return Result.ok(dictAdminService.listGroups());
    }

    /**
     * 按编码读取字典组。
     *
     * @param groupCode 字典组编码
     * @return 字典组
     */
    @GetMapping("/by-code/{groupCode}")
    @PreAuthorize("hasAuthority('" + SystemPermissions.Dict.LIST_READ + "')")
    public Result<DictGroupVO> getByCode(@PathVariable String groupCode) {
        return Result.ok(dictAdminService.getGroupByCode(groupCode));
    }

    /**
     * 按编码读取字典树。
     *
     * @param groupCode 字典组编码
     * @param enabledOnly 为 true 时只返回启用项
     * @return 字典树
     */
    @GetMapping("/by-code/{groupCode}/tree")
    @PreAuthorize("hasAuthority('" + SystemPermissions.Dict.LIST_READ + "')")
    public Result<List<DictItemTreeVO>> treeByCode(
            @PathVariable String groupCode, @RequestParam(defaultValue = "false") boolean enabledOnly) {
        return Result.ok(dictAdminService.listTreeByCode(groupCode, enabledOnly));
    }

    /**
     * 字典组详情。
     *
     * @param groupId 字典组 ID
     * @return 字典组
     */
    @GetMapping("/{groupId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).LIST_READ)")
    public Result<DictGroupVO> detail(@PathVariable Long groupId) {
        return Result.ok(dictAdminService.getGroup(groupId));
    }

    /**
     * 创建字典组。
     *
     * @param request 创建请求，maxLevel 为该组允许的层数
     * @return 新建字典组
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).CREATE)")
    public Result<DictGroupVO> create(@Valid @RequestBody CreateDictGroupRequest request) {
        return Result.ok(dictAdminService.createGroup(request));
    }

    /**
     * 更新字典组。
     *
     * @param groupId 字典组 ID
     * @param request 更新请求
     * @return 更新后的字典组
     */
    @PutMapping("/{groupId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).UPDATE)")
    public Result<DictGroupVO> update(@PathVariable Long groupId, @Valid @RequestBody UpdateDictGroupRequest request) {
        return Result.ok(dictAdminService.updateGroup(groupId, request));
    }

    /**
     * 删除字典组。
     *
     * @param groupId 字典组 ID
     * @return 空结果
     */
    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).DELETE)")
    public Result<Void> delete(@PathVariable Long groupId) {
        dictAdminService.deleteGroup(groupId);
        return Result.ok();
    }

    /**
     * 组内字典项列表。
     *
     * @param groupId 字典组 ID
     * @return 字典项
     */
    @GetMapping("/{groupId}/items")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).LIST_READ)")
    public Result<List<DictItemVO>> listItems(@PathVariable Long groupId) {
        return Result.ok(dictAdminService.listItems(groupId));
    }

    /**
     * 组内字典树。
     *
     * @param groupId 字典组 ID
     * @param enabledOnly 为 true 时只返回启用项
     * @return 字典树
     */
    @GetMapping("/{groupId}/tree")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).LIST_READ)")
    public Result<List<DictItemTreeVO>> tree(
            @PathVariable Long groupId, @RequestParam(defaultValue = "false") boolean enabledOnly) {
        return Result.ok(dictAdminService.listTree(groupId, enabledOnly));
    }

    /**
     * 创建字典项。
     *
     * @param groupId 字典组 ID
     * @param request 创建请求
     * @return 新建字典项
     */
    @PostMapping("/{groupId}/items")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).CREATE)")
    public Result<DictItemVO> createItem(
            @PathVariable Long groupId, @Valid @RequestBody CreateDictItemRequest request) {
        return Result.ok(dictAdminService.createItem(groupId, request));
    }

    /**
     * 更新字典项。
     *
     * @param groupId 字典组 ID
     * @param itemId 字典项 ID
     * @param request 更新请求
     * @return 更新后的字典项
     */
    @PutMapping("/{groupId}/items/{itemId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).UPDATE)")
    public Result<DictItemVO> updateItem(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateDictItemRequest request) {
        return Result.ok(dictAdminService.updateItem(groupId, itemId, request));
    }

    /**
     * 删除字典项。
     *
     * @param groupId 字典组 ID
     * @param itemId 字典项 ID
     * @return 空结果
     */
    @DeleteMapping("/{groupId}/items/{itemId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Dict).DELETE)")
    public Result<Void> deleteItem(@PathVariable Long groupId, @PathVariable Long itemId) {
        dictAdminService.deleteItem(groupId, itemId);
        return Result.ok();
    }
}
