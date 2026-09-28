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
 * 数据字典组。每个组自行声明允许的层数，取值为 1、2 或 3。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict_group")
public class SysDictGroup {

    /** 字典组主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 字典组编码 */
    private String groupCode;

    /** 字典组名称 */
    private String groupName;

    /** 允许的最大层数：1、2 或 3 */
    private Integer maxLevel;

    /** 备注 */
    private String remark;

    /** 排序号，升序 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
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
