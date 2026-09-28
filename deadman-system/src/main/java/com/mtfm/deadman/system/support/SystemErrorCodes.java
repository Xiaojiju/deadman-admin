package com.mtfm.deadman.system.support;

/**
 * 用户与组织模块业务码。数值与历史接口保持一致，文案键仍为 result.*，文案在本模块资源包。
 */
public final class SystemErrorCodes {

    /** 角色不存在 */
    public static final int ROLE_NOT_FOUND = 11001;

    /** 角色编码已存在 */
    public static final int ROLE_CODE_EXISTS = 11002;

    /** 系统内置角色不允许删除或禁用 */
    public static final int ROLE_SYSTEM_PROTECTED = 11003;

    /** 超级管理员角色不允许修改权限 */
    public static final int ROLE_SUPER_ADMIN_PROTECTED = 11004;

    /** 存在无效的权限码 */
    public static final int PERMISSION_INVALID = 11005;

    /** 部门不存在 */
    public static final int DEPARTMENT_NOT_FOUND = 12001;

    /** 部门编码已存在 */
    public static final int DEPARTMENT_CODE_EXISTS = 12002;

    /** 存在下级部门，无法删除 */
    public static final int DEPARTMENT_HAS_CHILDREN = 12003;

    /** 部门下存在用户，无法删除 */
    public static final int DEPARTMENT_HAS_USERS = 12004;

    /** 职位不存在 */
    public static final int POSITION_NOT_FOUND = 12011;

    /** 职位编码已存在 */
    public static final int POSITION_CODE_EXISTS = 12012;

    /** 职位下存在用户，无法删除 */
    public static final int POSITION_HAS_USERS = 12013;

    /** 职位所属部门与用户部门不一致 */
    public static final int POSITION_DEPT_MISMATCH = 12014;

    /** 字典组不存在 */
    public static final int DICT_GROUP_NOT_FOUND = 12101;

    /** 字典组编码已存在 */
    public static final int DICT_GROUP_CODE_EXISTS = 12102;

    /** 字典组下存在字典项，无法删除 */
    public static final int DICT_GROUP_HAS_ITEMS = 12103;

    /** 字典层数只能是 1、2 或 3 */
    public static final int DICT_LEVEL_INVALID = 12104;

    /** 字典项超过该组允许的层数 */
    public static final int DICT_LEVEL_EXCEEDED = 12105;

    /** 已有更深的字典项，不能缩小层数 */
    public static final int DICT_LEVEL_SHRINK_BLOCKED = 12106;

    /** 字典项不存在 */
    public static final int DICT_ITEM_NOT_FOUND = 12111;

    /** 字典项编码已存在 */
    public static final int DICT_ITEM_CODE_EXISTS = 12112;

    /** 存在下级字典项，无法删除 */
    public static final int DICT_ITEM_HAS_CHILDREN = 12113;

    /** 上级字典项无效 */
    public static final int DICT_ITEM_PARENT_INVALID = 12114;

    private SystemErrorCodes() {}
}
