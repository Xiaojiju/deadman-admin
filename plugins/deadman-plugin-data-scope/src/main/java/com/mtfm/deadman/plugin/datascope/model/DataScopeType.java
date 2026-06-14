package com.mtfm.deadman.plugin.datascope.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据范围类型，存储于 {@code user_data_scope.scope_type}，与用户角色独立配置。
 */
@Getter
@RequiredArgsConstructor
public enum DataScopeType {

    /** 全部数据 */
    ALL(100),

    /** 自定义部门/片区，可见部门见 {@code user_data_scope_dept} */
    CUSTOM(80),

    /** 本部门及下级部门 */
    DEPT_AND_CHILD(60),

    /** 仅本部门 */
    DEPT(40),

    /** 仅本人 */
    SELF(20);

    /** 范围优先级，数值越大范围越宽（保留供策略扩展） */
    private final int priority;

    /**
     * 按编码解析枚举，未知编码返回 null。
     *
     * @param code 枚举名或编码
     * @return 数据范围类型
     */
    public static DataScopeType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return DataScopeType.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
