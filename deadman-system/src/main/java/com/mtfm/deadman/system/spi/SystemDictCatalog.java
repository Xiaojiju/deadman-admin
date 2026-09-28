package com.mtfm.deadman.system.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.spi.DictCatalog;
import com.mtfm.deadman.common.spi.DictCatalogItem;
import com.mtfm.deadman.system.entity.SysDictGroup;
import com.mtfm.deadman.system.service.DictAdminService;
import com.mtfm.deadman.system.service.SysDictGroupService;
import com.mtfm.deadman.system.support.SystemErrorCodes;
import com.mtfm.deadman.system.vo.dict.DictItemTreeVO;

/**
 * 用系统字典实现 {@link DictCatalog}。
 */
@Component
public class SystemDictCatalog implements DictCatalog {

    private final SysDictGroupService sysDictGroupService;
    private final DictAdminService dictAdminService;

    /**
     * @param sysDictGroupService 字典组
     * @param dictAdminService 字典树
     */
    public SystemDictCatalog(SysDictGroupService sysDictGroupService, DictAdminService dictAdminService) {
        this.sysDictGroupService = sysDictGroupService;
        this.dictAdminService = dictAdminService;
    }

    /**
     * 停用或缺失的字典组视为没有目录。
     *
     * @param groupCode 字典组编码
     * @return 启用项
     */
    @Override
    public Optional<List<DictCatalogItem>> listEnabled(String groupCode) {
        SysDictGroup group;
        try {
            group = sysDictGroupService.requireByCode(groupCode);
        } catch (BusinessException ex) {
            if (ex.getCode() == SystemErrorCodes.DICT_GROUP_NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
        if (group.getStatus() == null || group.getStatus() != UserStatus.ACTIVE.getValue()) {
            return Optional.empty();
        }
        List<DictCatalogItem> items = new ArrayList<>();
        flatten(dictAdminService.listTree(group.getId(), true), items);
        return Optional.of(items);
    }

    private static void flatten(List<DictItemTreeVO> nodes, List<DictCatalogItem> items) {
        if (nodes == null) {
            return;
        }
        for (DictItemTreeVO node : nodes) {
            items.add(new DictCatalogItem(node.id(), node.parentId(), node.itemCode(), node.itemLabel(),
                node.itemLevel() == null ? 0 : node.itemLevel()));
            flatten(node.children(), items);
        }
    }
}
