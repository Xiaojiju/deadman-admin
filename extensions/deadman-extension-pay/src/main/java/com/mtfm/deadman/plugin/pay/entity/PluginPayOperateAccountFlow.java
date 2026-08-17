package com.mtfm.deadman.plugin.pay.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台运营账户（OPERATION）资金流水。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_operate_account_flow")
public class PluginPayOperateAccountFlow {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 流水号 */
    private String flowNo;

    /** 业务场景 */
    private String bizScene;

    /** 方向：IN/OUT */
    private String direction;

    /** 金额（分） */
    private Long amountCents;

    /** 支付平台：WECHAT/ALIPAY */
    private String payPlatform;

    /** 关联业务单号 */
    private String bizOrderNo;

    /** 关联渠道单号（如 out_bill_no） */
    private String channelRefNo;

    /** 备注 */
    private String remark;

    /** 幂等键 */
    private String idempotentKey;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
