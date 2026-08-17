package com.mtfm.deadman.plugin.ess.tencent.vo;

/**
 * 文件发起签署结果。
 *
 * @param flowId 合同流程 Id
 * @param schemeUrl 签署短链（可在移动端浏览器打开）
 * @param fileId 上传后的电子签文件资源 Id
 */
public record CreateFlowByFileResult(
        String flowId,
        String schemeUrl,
        String fileId) {
}
