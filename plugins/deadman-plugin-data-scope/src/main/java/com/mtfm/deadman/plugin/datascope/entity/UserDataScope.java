package com.mtfm.deadman.plugin.datascope.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户数据权限配置，与用户角色独立维护。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_data_scope")
public class UserDataScope {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户 ID，关联 user_base.id */
    private Long userId;

    /** 数据范围类型，参见 {@link com.mtfm.deadman.plugin.datascope.model.DataScopeType} */
    private String scopeType;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
