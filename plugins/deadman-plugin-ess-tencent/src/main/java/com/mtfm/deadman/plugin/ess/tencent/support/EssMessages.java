package com.mtfm.deadman.plugin.ess.tencent.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class EssMessages {

    private EssMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case EssErrorCodes.ESS_CONFIG_INVALID -> ex(code, "result.ESS_CONFIG_INVALID", "电子签插件配置无效");
            case EssErrorCodes.ESS_API_FAILED -> ex(code, "result.ESS_API_FAILED", "电子签 API 调用失败");
            case EssErrorCodes.ESS_UPLOAD_FAILED -> ex(code, "result.ESS_UPLOAD_FAILED", "电子签文件上传失败");
            case EssErrorCodes.ESS_CALLBACK_INVALID -> ex(code, "result.ESS_CALLBACK_INVALID", "电子签回调验签或解密失败");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
