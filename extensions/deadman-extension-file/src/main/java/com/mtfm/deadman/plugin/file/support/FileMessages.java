package com.mtfm.deadman.plugin.file.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class FileMessages {

    private FileMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case FileErrorCodes.FILE_NOT_FOUND -> ex(code, "result.FILE_NOT_FOUND", "文件不存在");
            case FileErrorCodes.FILE_TOO_LARGE -> ex(code, "result.FILE_TOO_LARGE", "文件大小超出限制");
            case FileErrorCodes.FILE_STORAGE_ERROR -> ex(code, "result.FILE_STORAGE_ERROR", "文件存储失败");
            case FileErrorCodes.FILE_PROVIDER_NOT_FOUND -> ex(code, "result.FILE_PROVIDER_NOT_FOUND", "文件存储 Provider 不存在");
            case FileErrorCodes.FILE_BIZ_TYPE_UNREGISTERED -> ex(code, "result.FILE_BIZ_TYPE_UNREGISTERED", "文件业务分类未注册");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
