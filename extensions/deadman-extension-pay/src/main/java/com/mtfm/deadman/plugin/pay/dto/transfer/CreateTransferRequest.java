package com.mtfm.deadman.plugin.pay.dto.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发起商家转账请求。
 *
 * @param bizOrderNo      业务单号
 * @param openid          收款用户 openid
 * @param amountCents     转账总金额（分）；超过单笔限额时自动拆单
 * @param transferSceneId 转账场景 ID
 * @param transferRemark  转账备注
 * @param userName        收款用户姓名（金额≥2000元时建议传入）
 * @param providerId      Transfer Provider，空则使用默认支付 Provider
 */
public record CreateTransferRequest(
        @NotBlank @Size(max = 64) String bizOrderNo,
        @NotBlank @Size(max = 128) String openid,
        @NotNull @Min(1) Long amountCents,
        @NotBlank @Size(max = 36) String transferSceneId,
        @NotBlank @Size(max = 32) String transferRemark,
        @Size(max = 64) String userName,
        @Size(max = 64) String providerId) {
}
