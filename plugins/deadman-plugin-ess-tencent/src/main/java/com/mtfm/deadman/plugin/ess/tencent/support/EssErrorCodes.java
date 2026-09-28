package com.mtfm.deadman.plugin.ess.tencent.support;

/**
 * 电子签模块业务码。数值与历史接口保持一致，文案键仍为 result.ESS_*。
 */
public final class EssErrorCodes {

    /** 电子签插件配置无效 */
    public static final int ESS_CONFIG_INVALID = 14501;

    /** 电子签 API 调用失败 */
    public static final int ESS_API_FAILED = 14502;

    /** 电子签文件上传失败 */
    public static final int ESS_UPLOAD_FAILED = 14503;

    /** 电子签回调验签或解密失败 */
    public static final int ESS_CALLBACK_INVALID = 14504;

    private EssErrorCodes() {}
}
