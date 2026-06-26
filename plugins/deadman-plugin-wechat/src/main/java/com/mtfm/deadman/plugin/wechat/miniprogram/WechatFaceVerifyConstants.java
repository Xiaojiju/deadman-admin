package com.mtfm.deadman.plugin.wechat.miniprogram;

/**
 * 微信小程序人脸核身常量。
 */
public final class WechatFaceVerifyConstants {

    /** 身份证证件类型 */
    public static final String CERT_TYPE_IDENTITY_CARD = "IDENTITY_CARD";

    /** 人脸核身验证成功 */
    public static final int VERIFY_RET_SUCCESS = 10000;

    /** 人脸核身进行中，需稍后重试查询 */
    public static final int VERIFY_RET_IN_PROGRESS = 10005;

    /** 未完成核身 */
    public static final int VERIFY_RET_NOT_COMPLETED = 10300;

    private WechatFaceVerifyConstants() {
    }
}
