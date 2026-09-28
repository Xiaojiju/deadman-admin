package com.mtfm.deadman.plugin.identity.enums;

/**
 * 认证结论。
 */
public enum CertificationStatus {

    /** 活体与人脸比对均达到配置阈值 */
    PASSED,
    /** 活体或人脸比对未通过 */
    REJECTED
}
