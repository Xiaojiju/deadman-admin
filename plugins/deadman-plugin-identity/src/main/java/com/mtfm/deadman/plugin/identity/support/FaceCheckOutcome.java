package com.mtfm.deadman.plugin.identity.support;

/**
 * 先活体、再比对之后的判定结果。
 *
 * @param livenessScore 活体分数
 * @param livenessRequestId 活体检测请求 ID
 * @param matchScore 人脸相似度；活体未通过时为空
 * @param compareRequestId 人脸比对请求 ID；未调用比对时为空
 * @param passed 是否达到配置阈值
 * @param rejectReason 未通过原因；通过时为空
 */
public record FaceCheckOutcome(float livenessScore, String livenessRequestId, Float matchScore, String compareRequestId,
    boolean passed, String rejectReason) {
}
