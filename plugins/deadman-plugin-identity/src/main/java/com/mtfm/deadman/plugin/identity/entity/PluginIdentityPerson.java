package com.mtfm.deadman.plugin.identity.entity;

import java.time.LocalDate;
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
 * 个人实名认证记录，同一业务域下每个主体只保留最新一次结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_identity_person")
public class PluginIdentityPerson {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务域，用于跨项目复用时隔离主体 */
    private String realm;

    /** 业务主体 ID，如用户端用户主键 */
    private Long subjectId;

    /** 真实姓名 */
    private String realName;

    /** 身份证号 */
    private String idCardNo;

    /** 性别：1 男，2 女 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 身份证人像面文件 ID */
    private Long idCardFrontFileId;

    /** 身份证国徽面文件 ID */
    private Long idCardBackFileId;

    /** 现场人脸文件 ID */
    private Long faceFileId;

    /** 活体分数 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Float livenessScore;

    /** 活体检测请求 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String livenessRequestId;

    /** 人脸相似度 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Float matchScore;

    /** 人脸比对请求 ID */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String compareRequestId;

    /** 认证状态：PASSED / REJECTED */
    private String status;

    /** 未通过原因 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String rejectReason;

    /** 最近一次判定时间 */
    private LocalDateTime verifiedTime;

    /** 逻辑删除：0 未删除，1 已删除 */
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
