package com.mtfm.deadman.plugin.ess.tencent.dto;

import java.util.List;

/**
 * 通过 PDF 文件发起签署流程请求。
 *
 * @param flowName 合同流程名称
 * @param fileName 上传文件名（需含扩展名，建议 .pdf）
 * @param fileBytes 文件二进制内容
 * @param approvers 签署方列表
 * @param operatorUserId 可选经办人，空则使用配置默认值
 * @param deadlineUnix 签署截止时间（Unix 秒），可空
 * @param unordered 是否无序签署，可空
 * @param userData 透传业务数据，可空
 */
public record CreateFlowByFileCommand(
        String flowName,
        String fileName,
        byte[] fileBytes,
        List<EssApproverCommand> approvers,
        String operatorUserId,
        Long deadlineUnix,
        Boolean unordered,
        String userData) {
}
