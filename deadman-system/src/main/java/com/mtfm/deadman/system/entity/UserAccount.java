package com.mtfm.deadman.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户登录账号表实体，一用户可绑定多种登录方式。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_account")
public class UserAccount {

    /** 账号记录主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联用户基础信息主键 {@link UserBase#id} */
    private Long userId;

    /** 账号类型：USERNAME / PHONE / OAUTH，参见 {@link com.mtfm.deadman.common.enums.AccountType} */
    private String accountType;

    /** 账号标识：用户名、手机号、OAuth subject 等 */
    private String accountIdentifier;

    /** 第三方 OAuth 提供商标识，如 wechat、github；非 OAuth 登录时为 null */
    private String oauthProvider;

    /** 第三方 OAuth 用户唯一标识（subject / openId） */
    private String oauthSubject;

    /** 扩展凭证元数据，JSON 字符串，如 token 摘要、绑定时间等 */
    private String credentialMeta;

    /** 是否已验证：0-未验证，1-已验证（如手机已验、邮箱已验） */
    private Integer verified;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
