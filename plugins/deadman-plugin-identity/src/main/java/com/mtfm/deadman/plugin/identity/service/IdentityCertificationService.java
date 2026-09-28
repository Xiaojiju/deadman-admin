package com.mtfm.deadman.plugin.identity.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.plugin.identity.support.IdentityErrorCodes;
import com.mtfm.deadman.plugin.identity.support.IdentityMessages;
import com.mtfm.deadman.plugin.identity.config.IdentityPluginProperties;
import com.mtfm.deadman.plugin.identity.dto.EnterpriseCertificationCommand;
import com.mtfm.deadman.plugin.identity.dto.PersonalCertificationCommand;
import com.mtfm.deadman.plugin.identity.entity.PluginIdentityEnterprise;
import com.mtfm.deadman.plugin.identity.entity.PluginIdentityPerson;
import com.mtfm.deadman.plugin.identity.enums.CertificationStatus;
import com.mtfm.deadman.plugin.identity.mapper.PluginIdentityEnterpriseMapper;
import com.mtfm.deadman.plugin.identity.mapper.PluginIdentityPersonMapper;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.spi.IdentityImageLoader;
import com.mtfm.deadman.plugin.identity.support.FaceCheckOutcome;
import com.mtfm.deadman.plugin.identity.support.IdCardSupport;
import com.mtfm.deadman.plugin.identity.support.IdCardSupport.ParsedIdCard;
import com.mtfm.deadman.plugin.identity.support.IdentityFaceGate;
import com.mtfm.deadman.plugin.identity.vo.EnterpriseCertificationView;
import com.mtfm.deadman.plugin.identity.vo.IdentitySettingsView;
import com.mtfm.deadman.plugin.identity.vo.PersonalCertificationView;

/**
 * 可复用的个人实名与企业认证流程。按 {@code realm + subjectId} 隔离，不绑定某一套用户表。
 */
@Service
public class IdentityCertificationService {

    private static final int MAX_IMAGE_BYTES = 3_500_000;

    private final IdentityPluginProperties properties;
    private final FaceRecognitionClient faceRecognitionClient;
    private final ObjectProvider<IdentityImageLoader> imageLoaders;
    private final PluginIdentityPersonMapper personMapper;
    private final PluginIdentityEnterpriseMapper enterpriseMapper;
    private final TransactionTemplate transactionTemplate;

    /**
     * 组装认证服务。人脸调用放在事务外，只有判定结果落库使用短事务。
     *
     * @param properties 插件配置
     * @param faceRecognitionClient 人脸通道
     * @param imageLoaders 图片读取器
     * @param personMapper 个人认证 Mapper
     * @param enterpriseMapper 企业认证 Mapper
     * @param transactionManager 事务管理器
     */
    public IdentityCertificationService(IdentityPluginProperties properties,
        FaceRecognitionClient faceRecognitionClient, ObjectProvider<IdentityImageLoader> imageLoaders,
        PluginIdentityPersonMapper personMapper, PluginIdentityEnterpriseMapper enterpriseMapper,
        PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.faceRecognitionClient = faceRecognitionClient;
        this.imageLoaders = imageLoaders;
        this.personMapper = personMapper;
        this.enterpriseMapper = enterpriseMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 读取认证开关。
     *
     * @return 当前配置
     */
    public IdentitySettingsView settings() {
        return new IdentitySettingsView(properties.isEnabled(), properties.isRealNameEnabled(),
            properties.isEnterpriseEnabled(), properties.isRequiredOnRegister(), properties.getMatchScoreThreshold(),
            properties.getLivenessScoreThreshold());
    }

    /**
     * 提交个人实名：校验证件号，确认活体后再与身份证人像面比对。
     *
     * @param realm 业务域
     * @param subjectId 业务主体
     * @param command 认证材料
     * @return 已落库的认证记录（含未通过的分数）
     */
    public PersonalCertificationView submitPersonal(String realm, Long subjectId,
        PersonalCertificationCommand command) {
        requireRealm(realm, subjectId);
        if (!properties.isRealNameEnabled()) {
            throw IdentityMessages.of(IdentityErrorCodes.DISABLED, "identity.disabled", "实名认证未开启");
        }
        if (command == null || !StringUtils.hasText(command.realName())) {
            throw IdentityMessages.badRequest("identity.real_name_required", "真实姓名不能为空");
        }
        ParsedIdCard idCard = IdCardSupport.parse(command.idCardNo(), command.birthDate(), command.gender());
        byte[] front = loadImage(command.idCardFrontFileId());
        loadImage(command.idCardBackFileId());
        byte[] face = loadImage(command.faceFileId());
        FaceCheckOutcome outcome = IdentityFaceGate.check(faceRecognitionClient, front, face,
            properties.getLivenessScoreThreshold(), properties.getMatchScoreThreshold());
        PluginIdentityPerson saved =
            transactionTemplate.execute(status -> upsertPerson(realm, subjectId, command, idCard, outcome));
        assertPassed(outcome);
        return toPersonalView(saved);
    }

    /**
     * 查询个人实名记录。
     *
     * @param realm 业务域
     * @param subjectId 业务主体
     * @return 记录，尚未认证时返回 null
     */
    public PersonalCertificationView findPersonal(String realm, Long subjectId) {
        requireRealm(realm, subjectId);
        return toPersonalView(findPerson(realm, subjectId));
    }

    /**
     * 提交企业认证。法人身份证正面与现场人脸按同一套活体、比对规则核验。
     *
     * @param realm 业务域
     * @param subjectId 业务主体
     * @param command 企业材料
     * @return 已落库的认证记录
     */
    public EnterpriseCertificationView submitEnterprise(String realm, Long subjectId,
        EnterpriseCertificationCommand command) {
        requireRealm(realm, subjectId);
        if (!properties.isEnterpriseEnabled()) {
            throw IdentityMessages.of(IdentityErrorCodes.DISABLED, "identity.enterprise_disabled", "企业认证未开启");
        }
        validateEnterprise(command);
        ParsedIdCard legalId = IdCardSupport.parse(command.legalIdCardNo(), null, null);
        loadImage(command.licenseFileId());
        byte[] front = loadImage(command.legalIdFrontFileId());
        loadImage(command.legalIdBackFileId());
        byte[] face = loadImage(command.legalFaceFileId());
        FaceCheckOutcome outcome = IdentityFaceGate.check(faceRecognitionClient, front, face,
            properties.getLivenessScoreThreshold(), properties.getMatchScoreThreshold());
        PluginIdentityEnterprise saved = transactionTemplate
            .execute(status -> upsertEnterprise(realm, subjectId, command, legalId.idCardNo(), outcome));
        assertPassed(outcome);
        return toEnterpriseView(saved);
    }

    /**
     * 查询企业认证记录。
     *
     * @param realm 业务域
     * @param subjectId 业务主体
     * @return 记录，尚未认证时返回 null
     */
    public EnterpriseCertificationView findEnterprise(String realm, Long subjectId) {
        requireRealm(realm, subjectId);
        return toEnterpriseView(findEnterpriseRow(realm, subjectId));
    }

    private void assertPassed(FaceCheckOutcome outcome) {
        if (outcome.passed()) {
            return;
        }
        if (outcome.matchScore() == null) {
            throw IdentityMessages.of(IdentityErrorCodes.LIVENESS_FAILED, "identity.liveness_failed",
                "活体检测未通过，分数 " + outcome.livenessScore(), outcome.livenessScore());
        }
        throw IdentityMessages.of(IdentityErrorCodes.FACE_MISMATCH, "identity.face_mismatch",
            "人脸比对未通过，相似度 " + outcome.matchScore(), outcome.matchScore());
    }

    private PluginIdentityPerson upsertPerson(String realm, Long subjectId, PersonalCertificationCommand command,
        ParsedIdCard idCard, FaceCheckOutcome outcome) {
        PluginIdentityPerson row = findPerson(realm, subjectId);
        if (row == null) {
            row = new PluginIdentityPerson();
            row.setRealm(realm);
            row.setSubjectId(subjectId);
        }
        row.setRealName(command.realName().trim());
        row.setIdCardNo(idCard.idCardNo());
        row.setGender(idCard.gender());
        row.setBirthDate(idCard.birthDate());
        row.setIdCardFrontFileId(command.idCardFrontFileId());
        row.setIdCardBackFileId(command.idCardBackFileId());
        row.setFaceFileId(command.faceFileId());
        fillOutcome(row, outcome);
        if (row.getId() == null) {
            personMapper.insert(row);
        } else {
            personMapper.updateById(row);
        }
        return row;
    }

    private PluginIdentityEnterprise upsertEnterprise(String realm, Long subjectId,
        EnterpriseCertificationCommand command, String legalIdCardNo, FaceCheckOutcome outcome) {
        PluginIdentityEnterprise row = findEnterpriseRow(realm, subjectId);
        if (row == null) {
            row = new PluginIdentityEnterprise();
            row.setRealm(realm);
            row.setSubjectId(subjectId);
        }
        row.setEnterpriseName(command.enterpriseName().trim());
        row.setCreditCode(command.creditCode().trim().toUpperCase());
        row.setAddress(command.address().trim());
        row.setLegalPersonName(command.legalPersonName().trim());
        row.setLegalIdCardNo(legalIdCardNo);
        row.setLicenseFileId(command.licenseFileId());
        row.setLegalIdFrontFileId(command.legalIdFrontFileId());
        row.setLegalIdBackFileId(command.legalIdBackFileId());
        row.setLegalFaceFileId(command.legalFaceFileId());
        row.setLivenessScore(outcome.livenessScore());
        row.setLivenessRequestId(outcome.livenessRequestId());
        row.setMatchScore(outcome.matchScore());
        row.setCompareRequestId(outcome.compareRequestId());
        row.setStatus(outcome.passed() ? CertificationStatus.PASSED.name() : CertificationStatus.REJECTED.name());
        row.setRejectReason(outcome.rejectReason());
        row.setVerifiedTime(LocalDateTime.now());
        if (row.getId() == null) {
            enterpriseMapper.insert(row);
        } else {
            enterpriseMapper.updateById(row);
        }
        return row;
    }

    private void fillOutcome(PluginIdentityPerson row, FaceCheckOutcome outcome) {
        row.setLivenessScore(outcome.livenessScore());
        row.setLivenessRequestId(outcome.livenessRequestId());
        row.setMatchScore(outcome.matchScore());
        row.setCompareRequestId(outcome.compareRequestId());
        row.setStatus(outcome.passed() ? CertificationStatus.PASSED.name() : CertificationStatus.REJECTED.name());
        row.setRejectReason(outcome.rejectReason());
        row.setVerifiedTime(LocalDateTime.now());
    }

    private byte[] loadImage(Long fileId) {
        if (fileId == null) {
            throw IdentityMessages.of(IdentityErrorCodes.IMAGE_INVALID, "identity.image_file_required", "图片文件不能为空");
        }
        IdentityImageLoader loader = imageLoaders.getIfAvailable();
        if (loader == null) {
            throw IdentityMessages.of(IdentityErrorCodes.CONFIG_INVALID, "identity.image_loader_missing", "未配置认证图片读取器");
        }
        byte[] bytes = loader.load(fileId);
        if (bytes == null || bytes.length == 0) {
            throw IdentityMessages.of(IdentityErrorCodes.IMAGE_INVALID, "identity.image_empty", "图片内容为空");
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw IdentityMessages.of(IdentityErrorCodes.IMAGE_INVALID, "identity.image_too_large",
                "图片过大，base64 后不能超过 5MB");
        }
        return bytes;
    }

    private void validateEnterprise(EnterpriseCertificationCommand command) {
        if (command == null || !StringUtils.hasText(command.enterpriseName())
            || !StringUtils.hasText(command.creditCode()) || !StringUtils.hasText(command.address())
            || !StringUtils.hasText(command.legalPersonName())) {
            throw IdentityMessages.badRequest("identity.enterprise_incomplete", "企业认证资料不完整");
        }
        String creditCode = command.creditCode().trim();
        if (!creditCode.matches("^[0-9A-Za-z]{18}$")) {
            throw IdentityMessages.badRequest("identity.credit_code_invalid", "统一社会信用代码须为 18 位字母或数字");
        }
    }

    private PluginIdentityPerson findPerson(String realm, Long subjectId) {
        return personMapper.selectOne(new LambdaQueryWrapper<PluginIdentityPerson>()
            .eq(PluginIdentityPerson::getRealm, realm).eq(PluginIdentityPerson::getSubjectId, subjectId));
    }

    private PluginIdentityEnterprise findEnterpriseRow(String realm, Long subjectId) {
        return enterpriseMapper.selectOne(new LambdaQueryWrapper<PluginIdentityEnterprise>()
            .eq(PluginIdentityEnterprise::getRealm, realm).eq(PluginIdentityEnterprise::getSubjectId, subjectId));
    }

    private void requireRealm(String realm, Long subjectId) {
        if (!StringUtils.hasText(realm) || subjectId == null) {
            throw IdentityMessages.badRequest("identity.subject_required", "认证主体不能为空");
        }
    }

    private PersonalCertificationView toPersonalView(PluginIdentityPerson row) {
        if (row == null) {
            return null;
        }
        return new PersonalCertificationView(row.getId(), row.getRealm(), row.getSubjectId(), row.getRealName(),
            row.getIdCardNo(), row.getGender(), row.getBirthDate(), row.getIdCardFrontFileId(),
            row.getIdCardBackFileId(), row.getFaceFileId(), row.getLivenessScore(), row.getMatchScore(),
            row.getStatus(), row.getRejectReason(), row.getVerifiedTime());
    }

    private EnterpriseCertificationView toEnterpriseView(PluginIdentityEnterprise row) {
        if (row == null) {
            return null;
        }
        return new EnterpriseCertificationView(row.getId(), row.getRealm(), row.getSubjectId(), row.getEnterpriseName(),
            row.getCreditCode(), row.getAddress(), row.getLegalPersonName(), row.getLegalIdCardNo(),
            row.getLicenseFileId(), row.getLegalIdFrontFileId(), row.getLegalIdBackFileId(), row.getLegalFaceFileId(),
            row.getLivenessScore(), row.getMatchScore(), row.getStatus(), row.getRejectReason(), row.getVerifiedTime());
    }
}
