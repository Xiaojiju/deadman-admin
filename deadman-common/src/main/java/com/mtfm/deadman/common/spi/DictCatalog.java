package com.mtfm.deadman.common.spi;

import java.util.List;
import java.util.Optional;

/**
 * 启用字典的只读目录。system 模块提供实现，业务组件通过本接口校验字典项，避免反向依赖 system。
 */
public interface DictCatalog {

    /**
     * 读取字典组下全部启用项，包含各级。禁用项及其下级不会出现。
     *
     * @param groupCode 字典组编码
     * @return 字典组不存在或已停用时为空；存在时返回启用项，可以是空列表
     */
    Optional<List<DictCatalogItem>> listEnabled(String groupCode);
}
