package com.mtfm.deadman.system.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class SystemMessages {

    private SystemMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case SystemErrorCodes.ROLE_NOT_FOUND -> ex(code, "result.ROLE_NOT_FOUND", "角色不存在");
            case SystemErrorCodes.ROLE_CODE_EXISTS -> ex(code, "result.ROLE_CODE_EXISTS", "角色编码已存在");
            case SystemErrorCodes.ROLE_SYSTEM_PROTECTED -> ex(code, "result.ROLE_SYSTEM_PROTECTED", "系统内置角色不允许删除或禁用");
            case SystemErrorCodes.ROLE_SUPER_ADMIN_PROTECTED -> ex(code, "result.ROLE_SUPER_ADMIN_PROTECTED", "超级管理员角色不允许修改权限");
            case SystemErrorCodes.PERMISSION_INVALID -> ex(code, "result.PERMISSION_INVALID", "存在无效的权限码");
            case SystemErrorCodes.DEPARTMENT_NOT_FOUND -> ex(code, "result.DEPARTMENT_NOT_FOUND", "部门不存在");
            case SystemErrorCodes.DEPARTMENT_CODE_EXISTS -> ex(code, "result.DEPARTMENT_CODE_EXISTS", "部门编码已存在");
            case SystemErrorCodes.DEPARTMENT_HAS_CHILDREN -> ex(code, "result.DEPARTMENT_HAS_CHILDREN", "存在下级部门，无法删除");
            case SystemErrorCodes.DEPARTMENT_HAS_USERS -> ex(code, "result.DEPARTMENT_HAS_USERS", "部门下存在用户，无法删除");
            case SystemErrorCodes.POSITION_NOT_FOUND -> ex(code, "result.POSITION_NOT_FOUND", "职位不存在");
            case SystemErrorCodes.POSITION_CODE_EXISTS -> ex(code, "result.POSITION_CODE_EXISTS", "职位编码已存在");
            case SystemErrorCodes.POSITION_HAS_USERS -> ex(code, "result.POSITION_HAS_USERS", "职位下存在用户，无法删除");
            case SystemErrorCodes.POSITION_DEPT_MISMATCH -> ex(code, "result.POSITION_DEPT_MISMATCH", "职位所属部门与用户部门不一致");
            case SystemErrorCodes.DICT_GROUP_NOT_FOUND -> ex(code, "result.DICT_GROUP_NOT_FOUND", "字典组不存在");
            case SystemErrorCodes.DICT_GROUP_CODE_EXISTS -> ex(code, "result.DICT_GROUP_CODE_EXISTS", "字典组编码已存在");
            case SystemErrorCodes.DICT_GROUP_HAS_ITEMS -> ex(code, "result.DICT_GROUP_HAS_ITEMS", "字典组下存在字典项，无法删除");
            case SystemErrorCodes.DICT_LEVEL_INVALID -> ex(code, "result.DICT_LEVEL_INVALID", "字典层数只能是 1、2 或 3");
            case SystemErrorCodes.DICT_LEVEL_EXCEEDED -> ex(code, "result.DICT_LEVEL_EXCEEDED", "字典项超过该组允许的层数");
            case SystemErrorCodes.DICT_LEVEL_SHRINK_BLOCKED -> ex(code, "result.DICT_LEVEL_SHRINK_BLOCKED", "已有更深的字典项，不能缩小层数");
            case SystemErrorCodes.DICT_ITEM_NOT_FOUND -> ex(code, "result.DICT_ITEM_NOT_FOUND", "字典项不存在");
            case SystemErrorCodes.DICT_ITEM_CODE_EXISTS -> ex(code, "result.DICT_ITEM_CODE_EXISTS", "字典项编码已存在");
            case SystemErrorCodes.DICT_ITEM_HAS_CHILDREN -> ex(code, "result.DICT_ITEM_HAS_CHILDREN", "存在下级字典项，无法删除");
            case SystemErrorCodes.DICT_ITEM_PARENT_INVALID -> ex(code, "result.DICT_ITEM_PARENT_INVALID", "上级字典项无效");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
