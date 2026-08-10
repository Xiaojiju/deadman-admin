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
 * 支付退款单，支持同一支付单的多次部分退款。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_refund")
public class PaymentRefundOrder {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台退款单号（商户侧 out_refund_no） */
    private String outRefundNo;

    /** 平台支付单号 */
    private String outTradeNo;

    /** 业务订单号 */
    private String bizOrderNo;

    /** 本次退款金额（分） */
    private Integer amountRefund;

    /** 原支付订单总金额（分） */
    private Integer amountTotal;

    /** 退款币种 */
    private String currency;

    /** 退款状态 */
    private String status;

    /** 支付平台 */
    private String payPlatform;

    /** 支付方式 */
    private String payMethod;

    /** Provider 标识 */
    private String providerId;

    /** 渠道退款单号 */
    private String channelRefundId;

    /** 渠道支付单号 */
    private String channelTransactionId;

    /** 退款原因 */
    private String reason;

    /** 退款入账账户描述 */
    private String userReceivedAccount;

    /** 最近一次回调/查单原文 */
    private String notifyRaw;

    /** 是否已发起过异常退款：0-否，1-是 */
    private Integer abnormalHandled;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
