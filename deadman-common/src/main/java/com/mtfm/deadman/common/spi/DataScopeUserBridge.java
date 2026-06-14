package com.mtfm.deadman.common.spi;

/**
 * 数据权限插件访问用户资料的 SPI，由 system 模块实现。
 */
public interface DataScopeUserBridge {

    /**
     * 校验用户存在，不存在时抛出业务异常。
     *
     * @param userId 用户 ID
     */
    void requireExists(Long userId);

    /**
     * 查询用户所属部门 ID。
     *
     * @param userId 用户 ID
     * @return 部门 ID，用户不存在或未分配部门时返回 null
     */
    Long findDepartmentId(Long userId);
}
