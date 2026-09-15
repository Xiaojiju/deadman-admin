package com.mtfm.deadman.plugin.ess.tencent.vo.channel;

/**
 * 渠道版按文件发起签署结果。
 *
 * @param flowId 合同流程 Id
 * @param buyerSchemeUrl 买家签署路径
 * @param merchantSchemeUrl 商户企业盖章路径
 */
public record ChannelCreateFlowByFilesResult(String flowId, String buyerSchemeUrl, String merchantSchemeUrl) {
}
