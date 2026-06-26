package com.mtfm.deadman.plugin.wechat.miniprogram.support;

import com.mtfm.deadman.plugin.wechat.miniprogram.WechatFaceVerifyConstants;

/**
 * 微信人脸核身 verify_ret 结果码描述支持。
 */
public final class WechatFaceVerifyRetSupport {

    private WechatFaceVerifyRetSupport() {
    }

    /**
     * 将 verify_ret 转为可读描述。
     *
     * @param verifyRet 微信核身结果码
     * @return 结果描述
     */
    public static String describe(int verifyRet) {
        return switch (verifyRet) {
            case WechatFaceVerifyConstants.VERIFY_RET_SUCCESS -> "识别成功";
            case WechatFaceVerifyConstants.VERIFY_RET_IN_PROGRESS -> "正在检测中";
            case WechatFaceVerifyConstants.VERIFY_RET_NOT_COMPLETED -> "未完成核身";
            case 10001 -> "参数错误";
            case 10002 -> "人脸特征检测失败";
            case 10003 -> "身份证号不匹配";
            case 10004 -> "比对人脸信息不匹配";
            case 10006 -> "appid 没有权限";
            case 10020 -> "认证超时";
            case 10042 -> "请求过于频繁，稍后再重试";
            case 90002, 90003, 90004, 90005, 90006 -> "用户取消";
            case 90007, 90012 -> "网络错误";
            case 90008 -> "相机权限未授权";
            case 90009 -> "麦克风权限未授权";
            case 90010 -> "相机和麦克风权限都未授权";
            case 90001, 90109 -> "设备不支持人脸检测";
            default -> "核身结果码 " + verifyRet;
        };
    }
}
