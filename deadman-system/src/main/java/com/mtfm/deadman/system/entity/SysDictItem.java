package com.mtfm.deadman.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * 数据字典项。层级由上级决定，且不能超过所属字典组的最大层数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict_item")
public class SysDictItem {

    /** 字典项主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 字典组主键 */
    private Long groupId;

    /** 上级字典项 ID，NULL 表示第 1 级。调整到根节点时需要把空值写回数据库 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long parentId;

    /** 组内唯一编码 */
    private String itemCode;

    /** 显示名称 */
    private String itemLabel;

    /** 业务取值，可与编码不同 */
    private String itemValue;

    /** 当前层级：1、2 或 3 */
    private Integer itemLevel;

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
