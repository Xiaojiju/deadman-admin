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
 * 支付平台单，统一记录各支付渠道预下单与支付结果。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_pay_order")
public class PaymentOrder {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台支付单号（商户侧 out_trade_no） */
    private String outTradeNo;

    /** 业务订单号 */
    private String bizOrderNo;

    /** 商品描述 */
    private String description;

    /** 订单金额（分） */
    private Integer amountTotal;

    /** 支付状态 */
    private String status;

    /** 支付平台，如 WECHAT、ALIPAY */
    private String payPlatform;

    /** 支付方式，如 JSAPI、NATIVE */
    private String payMethod;

    /** 支付 Provider 标识，如 wechat-jsapi */
    private String providerId;

    /** 渠道预支付 ID（如微信 prepay_id） */
    private String channelPrepayId;

    /** 渠道支付单号（如微信 transaction_id） */
    private String channelTransactionId;

    /** 渠道扩展信息 JSON（如 openid 等） */
    private String channelExtra;

    /** 付款人用户 ID（业务侧） */
    private Long payerUserId;

    /** 最近一次回调原文 */
    private String notifyRaw;

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
