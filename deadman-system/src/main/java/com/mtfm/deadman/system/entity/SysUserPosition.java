package com.mtfm.deadman.system.entity;

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
 * 用户与职位关联表实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_position")
public class SysUserPosition {

    /** 关联主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户主键 {@link UserBase#id} */
    private Long userId;

    /** 部门主键 {@link SysDepartment#id} */
    private Long departmentId;

    /** 职位主键 {@link SysPosition#id} */
    private Long positionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
