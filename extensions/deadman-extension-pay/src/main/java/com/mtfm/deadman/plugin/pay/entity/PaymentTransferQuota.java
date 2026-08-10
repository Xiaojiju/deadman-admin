package com.mtfm.deadman.plugin.pay.entity;

import java.time.LocalDateTime;

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

/**
 * 商家转账额度配置（API 维护，非 yaml）。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_transfer_quota")
public class PaymentTransferQuota {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商户主体类型 */
    private String merchantEntityType;

    /** 单笔限额（分） */
    private Long singleLimitCents;

    /** 单用户单日限额（分） */
    private Long userDailyLimitCents;

    /** 单日总额度（分） */
    private Long dailyLimitCents;

    /** 单月总额度（分，不可调） */
    private Long monthlyLimitCents;

    /** 是否允许派发待转：1-是，0-人工停止 */
    private Integer dispatchEnabled;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁 */
    @Version
    private Integer version;
}
