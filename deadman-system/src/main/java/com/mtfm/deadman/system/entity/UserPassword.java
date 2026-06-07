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
 * 用户密码表实体，每个用户仅允许一条有效密码记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_password")
public class UserPassword {

    /** 密码记录主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联用户基础信息主键 {@link UserBase#id}，表级唯一 */
    private Long userId;

    /** 密码哈希值，由对应 encoder 生成 */
    private String passwordHash;

    /** 编码器标识，对应 {@link com.mtfm.deadman.common.constants.PasswordEncoderIds} */
    private String encoderId;

    /** 密码版本号，每次改密递增 */
    private Integer passwordVersion;

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
