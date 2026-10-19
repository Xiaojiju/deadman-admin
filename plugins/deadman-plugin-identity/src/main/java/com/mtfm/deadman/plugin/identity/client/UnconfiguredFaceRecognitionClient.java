package com.mtfm.deadman.plugin.identity.client;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.support.IdentityErrorCodes;
import com.mtfm.deadman.plugin.identity.support.IdentityMessages;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionResult;

/**
 * 未配置腾讯云密钥时的占位实现，避免插件开关打开后应用无法启动。
 */
public class UnconfiguredFaceRecognitionClient implements FaceRecognitionClient {

    /**
     * 拒绝活体检测。
     *
     * @param faceImage 人脸图片
     * @return 不会返回
     */
    @Override
    public FaceRecognitionResult detectLive(byte[] faceImage) {
        throw notConfigured();
    }

    /**
     * 拒绝人脸比对。
     *
     * @param idCardImage 证件图片
     * @param faceImage 现场人脸
     * @return 不会返回
     */
    @Override
    public FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage) {
        throw notConfigured();
    }

    private static BusinessException notConfigured() {
        return IdentityMessages.of(IdentityErrorCodes.CONFIG_INVALID, "identity.secret_missing", "未配置腾讯云人脸识别密钥");
    }
}
