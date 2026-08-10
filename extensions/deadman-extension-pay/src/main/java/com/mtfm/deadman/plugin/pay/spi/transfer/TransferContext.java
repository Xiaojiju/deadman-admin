package com.mtfm.deadman.plugin.pay.spi.transfer;

import lombok.Builder;
import lombok.Getter;

/**
 * 渠道转账申请上下文。
 */
@Getter
@Builder
public class TransferContext {

    /** 业务单号 */
    private final String bizOrderNo;

    /** 收款用户 openid */
    private final String openid;

    /** 收款用户姓名明文（金额≥2000元时渠道侧必填且需加密） */
    private final String userName;

    /** 本笔金额（分） */
    private final long amountCents;

    /** 转账场景 ID */
    private final String transferSceneId;

    /** 转账备注 */
    private final String transferRemark;

    /** 渠道回调 URL（可选） */
    private final String notifyUrl;
}
