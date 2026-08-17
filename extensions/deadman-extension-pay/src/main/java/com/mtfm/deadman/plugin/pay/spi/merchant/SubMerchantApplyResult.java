package com.mtfm.deadman.plugin.pay.spi.merchant;

/**
 * 二级商户进件结果。
 *
 * @param outRequestNo    业务申请单号
 * @param channelApplyId  渠道进件单号（可空）
 * @param subMchid        二级商户号（通过后有值）
 * @param applymentState  进件状态（渠道原文或标准化）
 * @param rawPayload      渠道原文（可空）
 */
public record SubMerchantApplyResult(
        String outRequestNo,
        String channelApplyId,
        String subMchid,
        String applymentState,
        String rawPayload) {
}
