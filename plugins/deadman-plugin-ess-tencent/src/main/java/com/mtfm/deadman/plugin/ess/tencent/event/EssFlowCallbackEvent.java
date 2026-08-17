package com.mtfm.deadman.plugin.ess.tencent.event;

/**
 * 腾讯电子签流程回调事件（插件验签解密后发布，业务模块按需监听）。
 * <p>
 * 多业务共用同一回调通道时，可通过 {@code bizType}/{@code bizRef}（来自 UserData 约定
 * {@code bizType|bizRef}）或自行按 {@code flowId} 查本域单据进行分类处理。
 *
 * @param msgType      回调消息类型（如 FlowStatusChange）
 * @param flowId       签署流程 Id
 * @param flowStatus   流程状态（如 ALL / PART / REJECT 等）
 * @param userData     发起合同时透传的 UserData 原文
 * @param bizType      从 UserData 解析的业务类型，无法解析时为 null
 * @param bizRef       从 UserData 解析的业务引用；无约定格式时回落为 userData 原文
 * @param plainPayload 解密后的完整明文 JSON
 */
public record EssFlowCallbackEvent(
        String msgType,
        String flowId,
        String flowStatus,
        String userData,
        String bizType,
        String bizRef,
        String plainPayload) {
}
