package com.mtfm.deadman.plugin.im.tencent.support;

/**
 * 即时通讯模块业务码。数值与历史接口保持一致，文案键仍为 result.IM_*。
 */
public final class ImErrorCodes {

    /** IM 插件配置无效 */
    public static final int IM_CONFIG_INVALID = 14301;

    /** IM 用户域未注册 */
    public static final int IM_REALM_UNKNOWN = 14302;

    /** IM 用户已禁用 */
    public static final int IM_USER_DISABLED = 14303;

    /** IM 账号同步失败 */
    public static final int IM_ACCOUNT_SYNC_FAILED = 14304;

    /** IM 用户映射不存在 */
    public static final int IM_USER_NOT_FOUND = 14305;

    private ImErrorCodes() {}
}
