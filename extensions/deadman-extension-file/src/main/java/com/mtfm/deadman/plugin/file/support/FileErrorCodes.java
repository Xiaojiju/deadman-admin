package com.mtfm.deadman.plugin.file.support;

/**
 * 文件模块业务码。数值与历史接口保持一致，文案键仍为 result.FILE_*。
 */
public final class FileErrorCodes {

    /** 文件不存在 */
    public static final int FILE_NOT_FOUND = 13001;

    /** 文件大小超出限制 */
    public static final int FILE_TOO_LARGE = 13002;

    /** 文件存储失败 */
    public static final int FILE_STORAGE_ERROR = 13003;

    /** 文件存储 Provider 不存在 */
    public static final int FILE_PROVIDER_NOT_FOUND = 13004;

    /** 文件业务分类未注册 */
    public static final int FILE_BIZ_TYPE_UNREGISTERED = 13005;

    private FileErrorCodes() {}
}
