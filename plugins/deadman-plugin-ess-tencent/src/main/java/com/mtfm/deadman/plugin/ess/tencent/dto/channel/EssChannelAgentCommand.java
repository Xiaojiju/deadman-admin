package com.mtfm.deadman.plugin.ess.tencent.dto.channel;

/**
 * 渠道版电子签代理发起方信息。
 *
 * @param proxyOrganizationOpenId 代理企业 OpenId（子客企业标识）
 * @param proxyOperatorOpenId 代理经办人 OpenId（子客员工标识）
 */
public record EssChannelAgentCommand(
        String proxyOrganizationOpenId,
        String proxyOperatorOpenId) {
}
