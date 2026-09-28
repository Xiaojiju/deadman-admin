package com.mtfm.deadman.plugin.identity.vo;

import java.time.LocalDateTime;

/**
 * 企业认证记录。法人身份证号为原文，展示层负责脱敏。
 *
 * @param id 记录主键
 * @param realm 业务域
 * @param subjectId 业务主体 ID
 * @param enterpriseName 企业名称
 * @param creditCode 统一社会信用代码
 * @param address 企业地址
 * @param legalPersonName 法人姓名
 * @param legalIdCardNo 法人身份证号原文
 * @param licenseFileId 营业执照文件 ID
 * @param legalIdFrontFileId 法人身份证人像面文件 ID
 * @param legalIdBackFileId 法人身份证国徽面文件 ID
 * @param legalFaceFileId 法人现场人脸文件 ID
 * @param livenessScore 法人活体分数
 * @param matchScore 法人人脸相似度
 * @param status 认证状态
 * @param rejectReason 未通过原因
 * @param verifiedTime 最近一次判定时间
 */
public record EnterpriseCertificationView(Long id, String realm, Long subjectId, String enterpriseName,
    String creditCode, String address, String legalPersonName, String legalIdCardNo, Long licenseFileId,
    Long legalIdFrontFileId, Long legalIdBackFileId, Long legalFaceFileId, Float livenessScore, Float matchScore,
    String status, String rejectReason, LocalDateTime verifiedTime) {
}
