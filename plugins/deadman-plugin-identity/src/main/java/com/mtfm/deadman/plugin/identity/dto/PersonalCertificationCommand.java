package com.mtfm.deadman.plugin.identity.dto;

import java.time.LocalDate;

/**
 * 个人实名认证命令。图片通过文件主键交给 {@code IdentityImageLoader} 读取。
 *
 * @param realName 真实姓名
 * @param idCardNo 身份证号
 * @param gender 性别：1 男，2 女；为空时按身份证号解析
 * @param birthDate 出生日期；为空时按身份证号解析
 * @param idCardFrontFileId 身份证人像面文件 ID
 * @param idCardBackFileId 身份证国徽面文件 ID
 * @param faceFileId 现场人脸照片文件 ID
 */
public record PersonalCertificationCommand(String realName, String idCardNo, Integer gender, LocalDate birthDate,
    Long idCardFrontFileId, Long idCardBackFileId, Long faceFileId) {
}
