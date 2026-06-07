package com.mtfm.deadman.component.client.entity;

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
 * 用户端登录账号表实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_user_account")
public class ClientUserAccount {

    /** 账号记录主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联用户基础信息主键 */
    private Long userId;

    /** 账号类型：USERNAME / PHONE / OAUTH */
    private String accountType;

    /** 账号标识：用户名、手机号、OAuth subject 等 */
    private String accountIdentifier;

    /** 第三方 OAuth 提供商标识 */
    private String oauthProvider;

    /** 第三方 OAuth 用户唯一标识 */
    private String oauthSubject;

    /** 扩展凭证元数据 JSON */
    private String credentialMeta;

    /** 是否已验证：0-未验证，1-已验证 */
    private Integer verified;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
