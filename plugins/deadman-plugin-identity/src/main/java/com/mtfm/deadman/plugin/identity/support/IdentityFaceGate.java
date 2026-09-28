package com.mtfm.deadman.plugin.identity.support;

import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionResult;

/**
 * 认证顺序：先做高精度静态活体，通过后再做人脸比对，最后用可配置分数判定是否为同一人。
 */
public final class IdentityFaceGate {

    private IdentityFaceGate() {}

    /**
     * 执行活体检测与人脸比对。
     *
     * @param client 人脸通道
     * @param idCardImage 证件图片
     * @param faceImage 现场人脸图片
     * @param livenessThreshold 活体通过分
     * @param matchThreshold 同一人通过分
     * @return 判定结果。活体未通过时不会调用比对
     */
    public static FaceCheckOutcome check(FaceRecognitionClient client, byte[] idCardImage, byte[] faceImage,
        float livenessThreshold, float matchThreshold) {
        FaceRecognitionResult live = client.detectLive(faceImage);
        if (live.score() < livenessThreshold) {
            return new FaceCheckOutcome(live.score(), live.requestId(), null, null, false, "活体检测未通过");
        }
        FaceRecognitionResult compared = client.compare(idCardImage, faceImage);
        if (compared.score() < matchThreshold) {
            return new FaceCheckOutcome(live.score(), live.requestId(), compared.score(), compared.requestId(), false,
                "人脸比对未通过");
        }
        return new FaceCheckOutcome(live.score(), live.requestId(), compared.score(), compared.requestId(), true, null);
    }
}
