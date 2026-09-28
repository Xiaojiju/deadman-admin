package com.mtfm.deadman.plugin.identity.dto;

/**
 * 企业认证命令。法人需单独完成活体检测与身份证正面人脸比对。
 *
 * @param enterpriseName 企业名称
 * @param creditCode 统一社会信用代码
 * @param address 企业地址
 * @param legalPersonName 法人姓名
 * @param legalIdCardNo 法人身份证号
 * @param licenseFileId 营业执照文件 ID
 * @param legalIdFrontFileId 法人身份证人像面文件 ID
 * @param legalIdBackFileId 法人身份证国徽面文件 ID
 * @param legalFaceFileId 法人现场人脸照片文件 ID
 */
public record EnterpriseCertificationCommand(String enterpriseName, String creditCode, String address,
    String legalPersonName, String legalIdCardNo, Long licenseFileId, Long legalIdFrontFileId, Long legalIdBackFileId,
    Long legalFaceFileId) {
}
