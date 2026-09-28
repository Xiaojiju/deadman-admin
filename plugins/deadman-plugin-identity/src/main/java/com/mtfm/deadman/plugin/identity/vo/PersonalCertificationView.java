package com.mtfm.deadman.plugin.identity.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人实名认证记录。证件号为原文，展示层负责脱敏。
 *
 * @param id 记录主键
 * @param realm 业务域
 * @param subjectId 业务主体 ID
 * @param realName 真实姓名
 * @param idCardNo 身份证号原文
 * @param gender 性别：1 男，2 女
 * @param birthDate 出生日期
 * @param idCardFrontFileId 身份证人像面文件 ID
 * @param idCardBackFileId 身份证国徽面文件 ID
 * @param faceFileId 现场人脸文件 ID
 * @param livenessScore 活体分数
 * @param matchScore 人脸相似度
 * @param status 认证状态
 * @param rejectReason 未通过原因
 * @param verifiedTime 最近一次判定时间
 */
public record PersonalCertificationView(Long id, String realm, Long subjectId, String realName, String idCardNo,
    Integer gender, LocalDate birthDate, Long idCardFrontFileId, Long idCardBackFileId, Long faceFileId,
    Float livenessScore, Float matchScore, String status, String rejectReason, LocalDateTime verifiedTime) {
}
