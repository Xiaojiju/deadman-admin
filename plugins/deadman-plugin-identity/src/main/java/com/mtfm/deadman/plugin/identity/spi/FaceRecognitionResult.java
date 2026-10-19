package com.mtfm.deadman.plugin.identity.spi;

/**
 * 腾讯云人脸接口的单次调用结果。
 *
 * @param score 分数。活体与比对在 3.0 模型下均为 0–100
 * @param requestId 腾讯云请求 ID，便于排障
 */
public record FaceRecognitionResult(float score, String requestId) {
}
