package com.mtfm.deadman.plugin.pay.spi.refund;

import lombok.Builder;
import lombok.Getter;

/**
 * 异常退款申请上下文。
 * <p>
 * 当退款状态为 {@code ABNORMAL} 时，调用渠道「发起异常退款」接口。
 */
@Getter
@Builder
public class AbnormalRefundContext {

    /** 平台退款单号 */
    private final String outRefundNo;

    /** 渠道退款单号（如微信 refund_id） */
    private final String channelRefundId;

    /**
     * 异常退款入账方式：
     * {@link com.mtfm.deadman.plugin.pay.constant.AbnormalRefundReceiveType#MERCHANT_BANK_CARD} 或
     * {@link com.mtfm.deadman.plugin.pay.constant.AbnormalRefundReceiveType#USER_BANK_CARD}
     */
    private final String receiveType;

    /** 开户银行类型（退至用户银行卡时必填，如 ICBC_DEBIT） */
    private final String bankType;

    /** 收款银行卡号明文（退至用户银行卡时必填，渠道侧加密） */
    private final String bankAccount;

    /** 收款用户姓名明文（退至用户银行卡时必填，渠道侧加密） */
    private final String realName;
}
