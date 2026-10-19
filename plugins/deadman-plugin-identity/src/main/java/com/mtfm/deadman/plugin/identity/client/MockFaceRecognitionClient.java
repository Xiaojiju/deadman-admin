package com.mtfm.deadman.plugin.identity.client;

import java.util.UUID;

import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 未接入腾讯云时的联调实现。活体和人脸比对直接按通过分数返回，不发起外部请求。
 */
@Slf4j
public class MockFaceRecognitionClient implements FaceRecognitionClient {

    /** 高于默认活体阈值 40 和比对阈值 70，保证联调直接通过 */
    public static final float MOCK_SCORE = 99F;

    /**
     * 装配时打出警告，避免联调开关被带到生产环境后无人察觉。
     */
    public MockFaceRecognitionClient() {
        log.warn("deadman.plugin.identity.mock=true：人脸识别使用 mock，活体与比对直接通过。仅限未接入阶段，生产必须关闭");
    }

    /**
     * 跳过活体检测，直接返回通过分数。
     *
     * @param faceImage 人脸图片，本实现不读取内容
     * @return 固定通过结果
     */
    @Override
    public FaceRecognitionResult detectLive(byte[] faceImage) {
        return new FaceRecognitionResult(MOCK_SCORE, "mock-live-" + UUID.randomUUID());
    }

    /**
     * 跳过人脸比对，直接返回通过分数。
     *
     * @param idCardImage 证件图片，本实现不读取内容
     * @param faceImage 现场人脸，本实现不读取内容
     * @return 固定通过结果
     */
    @Override
    public FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage) {
        return new FaceRecognitionResult(MOCK_SCORE, "mock-compare-" + UUID.randomUUID());
    }
}
