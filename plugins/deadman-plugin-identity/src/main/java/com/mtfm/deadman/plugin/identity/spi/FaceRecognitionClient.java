package com.mtfm.deadman.plugin.identity.spi;

/**
 * 人脸活体与比对通道。默认实现调用腾讯云人脸识别。
 */
public interface FaceRecognitionClient {

    /**
     * 对一张人脸图做高精度静态活体检测。
     *
     * @param faceImage 人脸图片字节
     * @return 活体分数与请求 ID
     */
    FaceRecognitionResult detectLive(byte[] faceImage);

    /**
     * 比对两张图片中的人脸是否为同一人。调用方应先完成活体检测。
     *
     * @param idCardImage 证件照（通常为身份证正面）
     * @param faceImage 现场人脸照
     * @return 相似度分数与请求 ID
     */
    FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage);
}
