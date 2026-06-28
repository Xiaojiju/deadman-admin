package com.mtfm.deadman.component.openauth.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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

import java.time.LocalDateTime;

/**
 * 开放授权应用实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("open_app")
public class OpenApp {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 对外 AppId */
    private String appId;

    /** 应用名称 */
    private String appName;

    /** client_secret 哈希 */
    private String appSecretHash;

    /** 密钥哈希编码器 ID */
    private String secretEncoderId;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 应用说明 */
    private String description;

    /** 允许的用户域，逗号分隔 */
    private String allowedRealms;

    /** 默认 scope 列表，逗号分隔 */
    private String defaultScopes;

    /** auth_code 有效期（秒） */
    private Integer codeTtlSec;

    /** open_access_token 有效期（秒） */
    private Integer tokenTtlSec;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
