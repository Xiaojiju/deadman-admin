package com.mtfm.deadman.common.spi;

/**
 * 字典目录中的一条启用项。由 system 模块实现 {@link DictCatalog} 后提供，业务组件不直接依赖字典表。
 *
 * @param id 字典项主键
 * @param parentId 上级字典项 ID，第 1 级为空
 * @param itemCode 组内编码
 * @param itemLabel 显示名称
 * @param itemLevel 当前层级，从 1 开始
 */
public record DictCatalogItem(Long id, Long parentId, String itemCode, String itemLabel, int itemLevel) {
}
