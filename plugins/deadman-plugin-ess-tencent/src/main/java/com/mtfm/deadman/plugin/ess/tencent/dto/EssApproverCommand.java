package com.mtfm.deadman.plugin.ess.tencent.dto;

import java.util.List;

/**
 * 签署方命令。
 *
 * @param approverType 签署人类型：0 企业；1 个人；3 企业静默签
 * @param approverName 签署人姓名
 * @param approverMobile 签署人手机号
 * @param organizationName 企业名称（企业签署时必填）
 * @param notifyType 通知方式，如 sms / none
 * @param signComponents 签署控件列表
 */
public record EssApproverCommand(
        Long approverType,
        String approverName,
        String approverMobile,
        String organizationName,
        String notifyType,
        List<EssSignComponentCommand> signComponents) {
}
