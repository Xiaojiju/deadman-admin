package com.mtfm.deadman.plugin.ess.tencent.vo.channel;

/**
 * 渠道子客企业状态。
 *
 * @param organizationOpenId 子客企业 OpenId
 * @param organizationName 企业名称
 * @param activeStatus 激活状态：1-已激活可用
 * @param licenseExpireTime 许可到期 Unix 秒，可空
 * @param authorizationStatus 授权状态原文
 */
public record ChannelOrganizationStatusVO(
        String organizationOpenId,
        String organizationName,
        Long activeStatus,
        Long licenseExpireTime,
        String authorizationStatus) {

    /**
     * 子客是否处于可用激活状态。
     *
     * @return ActiveStatus=1 时返回 true
     */
    public boolean activated() {
        return activeStatus != null && activeStatus == 1L;
    }
}
