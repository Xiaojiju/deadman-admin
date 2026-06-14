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
 * 用户 CUSTOM 数据权限可见部门关联。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_data_scope_dept")
public class UserDataScopeDept {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户 ID，关联 user_base.id */
    private Long userId;

    /** 可见部门 ID，关联 sys_department.id */
    private Long deptId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
