package com.mtfm.deadman.plugin.ess.tencent.event;

/**
 * 腾讯电子签子客企业开通/实名回调（MsgType=OrgOpenTsignBiz）。
 *
 * @param proxyOrganizationOpenId 子客企业 OpenId
 * @param openSuccess 是否开通/认证成功
 * @param plainPayload 解密后的完整明文 JSON
 */
public record EssOrgAuthCallbackEvent(
        String proxyOrganizationOpenId,
        boolean openSuccess,
        String plainPayload) {
}
