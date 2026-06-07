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
 * 用户端密码表实体，一用户一条。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_user_password")
public class ClientUserPassword {

    /** 密码记录主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联用户主键 */
    private Long userId;

    /** 密码哈希 */
    private String passwordHash;

    /** 密码编码器标识 */
    private String encoderId;

    /** 密码版本号 */
    private Integer passwordVersion;

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
