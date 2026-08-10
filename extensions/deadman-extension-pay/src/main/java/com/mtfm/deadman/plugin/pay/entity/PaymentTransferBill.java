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
 * 商家转账明细（按单笔限额拆分后的待转/在途/终态单据）。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_transfer_bill")
public class PaymentTransferBill {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台转账单号 */
    private String outBillNo;

    /** 所属批次号 */
    private String batchNo;

    /** 业务单号 */
    private String bizOrderNo;

    /** 批次内序号 */
    private Integer seqNo;

    /** Provider 标识 */
    private String providerId;

    /** 支付平台 */
    private String payPlatform;

    /** 收款用户 openid */
    private String openid;

    /** 收款用户姓名 */
    private String userName;

    /** 本笔金额（分） */
    private Long amountCents;

    /** 转账场景 ID */
    private String transferSceneId;

    /** 转账备注 */
    private String transferRemark;

    /** 明细状态 */
    private String status;

    /** 渠道转账单号 */
    private String channelBillNo;

    /** 用户确认收款 package */
    private String packageInfo;

    /** 失败原因 */
    private String failReason;

    /** 回调/查单原文 */
    private String notifyRaw;

    /** 实际发起渠道时间 */
    private LocalDateTime dispatchedTime;

    /** 终态时间 */
    private LocalDateTime finishedTime;

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
