package com.mtfm.deadman.plugin.identity.support;

/**
 * 实名认证业务码。文案在本插件资源包，不进入 common 的 ResultCode。
 */
public final class IdentityErrorCodes {

    /** 实名或企业认证未开启 */
    public static final int DISABLED = 14601;

    /** 密钥或图片读取器未配置 */
    public static final int CONFIG_INVALID = 14602;

    /** 活体未通过 */
    public static final int LIVENESS_FAILED = 14603;

    /** 人脸比对未通过 */
    public static final int FACE_MISMATCH = 14604;

    /** 图片无效 */
    public static final int IMAGE_INVALID = 14605;

    /** 人脸识别服务调用失败 */
    public static final int API_FAILED = 14606;

    private IdentityErrorCodes() {}
}
