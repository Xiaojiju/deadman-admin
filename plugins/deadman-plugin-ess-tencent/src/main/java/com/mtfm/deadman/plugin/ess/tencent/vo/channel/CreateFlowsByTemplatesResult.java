package com.mtfm.deadman.plugin.ess.tencent.vo.channel;

/**
 * 渠道版模板发起签署结果。
 *
 * @param flowId 合同流程 Id
 * @param schemeUrl 签署链接或小程序路径（来自 CreateSignUrls）
 * @param errorMessage 发起过程中的错误信息；成功时为 null
 */
public record CreateFlowsByTemplatesResult(
        String flowId,
        String schemeUrl,
        String errorMessage) {
}
