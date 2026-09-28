package com.mtfm.deadman.plugin.identity.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业认证记录，同一业务域下每个主体只保留最新一次结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_identity_enterprise")
public class PluginIdentityEnterprise {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务域 */
    private String realm;

    /** 业务主体 ID */
    private Long subjectId;

    /** 企业名称 */
    private String enterpriseName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 企业地址 */
    private String address;

    /** 法人姓名 */
    private String legalPersonName;

    /** 法人身份证号 */
    private String legalIdCardNo;

    /** 营业执照文件 ID */
    private Long licenseFileId;

    /** 法人身份证人像面文件 ID */
    private Long legalIdFrontFileId;

    /** 法人身份证国徽面文件 ID */
    private Long legalIdBackFileId;

    /** 法人现场人脸文件 ID */
    private Long legalFaceFileId;

    /** 法人活体分数 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Float livenessScore;

    /** 法人活体检测请求 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String livenessRequestId;

    /** 法人人脸相似度 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Float matchScore;

    /** 法人人脸比对请求 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String compareRequestId;

    /** 认证状态：PASSED / REJECTED */
    private String status;

    /** 未通过原因 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String rejectReason;

    /** 最近一次判定时间 */
    private LocalDateTime verifiedTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
