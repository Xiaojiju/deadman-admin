package com.mtfm.deadman.plugin.ess.tencent.dto.channel;

import java.util.List;

import com.mtfm.deadman.plugin.ess.tencent.dto.EssSignComponentCommand;

/**
 * 渠道版按文件发起签署流程请求。
 *
 * @param agent 代理发起方（子客企业与经办人 OpenId）
 * @param flowName 合同流程名称
 * @param fileId 已上传的腾讯文件 Id
 * @param approvers 签署方（含各自签章控件；顺序即签署顺序）
 * @param customerData 业务透传数据
 * @param deadlineUnix 签署截止 Unix 秒，可空
 * @param unordered 是否无序签署；有序签署传 false
 */
public record ChannelCreateFlowByFilesCommand(
        EssChannelAgentCommand agent,
        String flowName,
        String fileId,
        List<EssChannelApproverCommand> approvers,
        String customerData,
        Long deadlineUnix,
        boolean unordered) {

    /**
     * 签署方控件列表便捷读取。
     *
     * @param approver 签署方
     * @return 控件列表，可空
     */
    public static List<EssSignComponentCommand> componentsOf(EssChannelApproverCommand approver) {
        return approver == null ? List.of() : approver.signComponents();
    }
}
