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
 * 商家转账批次。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_transfer_batch")
public class PaymentTransferBatch {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台转账批次号 */
    private String batchNo;

    /** 业务单号 */
    private String bizOrderNo;

    /** Provider 标识 */
    private String providerId;

    /** 支付平台 */
    private String payPlatform;

    /** 收款用户 openid */
    private String openid;

    /** 收款用户姓名 */
    private String userName;

    /** 批次总金额（分） */
    private Long amountTotalCents;

    /** 已成功金额（分） */
    private Long amountSuccessCents;

    /** 转账场景 ID */
    private String transferSceneId;

    /** 转账备注 */
    private String transferRemark;

    /** 批次状态 */
    private String status;

    /** 拆单笔数 */
    private Integer billCount;

    /** 成功笔数 */
    private Integer successCount;

    /** 失败笔数 */
    private Integer failCount;

    /** 待转笔数 */
    private Integer pendingCount;

    /** 停止原因 */
    private String stopReason;

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
