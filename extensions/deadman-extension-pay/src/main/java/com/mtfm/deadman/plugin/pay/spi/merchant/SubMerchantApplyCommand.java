package com.mtfm.deadman.plugin.pay.spi.merchant;

import java.util.Collections;
import java.util.Map;

/**
 * 二级商户进件命令（渠道无关核心字段 + 扩展 Map）。
 * <p>
 * 对齐微信收付通「提交申请单」始终必填：
 * {@code out_request_no} / {@code organization_type} / {@code account_info} /
 * {@code contact_info} / {@code sales_scene_info} / {@code merchant_shortname}。
 * 其余（营业执照、身份证照片 MediaID 等）按主体类型写入 {@link #channelParams()}，见
 * {@link SubMerchantChannelParams}。
 *
 * @param outRequestNo      业务申请单号（对应 out_request_no）
 * @param organizationType  主体类型（对应 organization_type：2401/2500/4/2/3/2502/1708）
 * @param merchantShortname 商户简称（对应 merchant_shortname）
 * @param channelParams     渠道扩展（结算账户、超管、经营场景、执照/证件等）
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012713017">提交申请单</a>
 */
public record SubMerchantApplyCommand(
        String outRequestNo,
        String organizationType,
        String merchantShortname,
        Map<String, String> channelParams) {

    /**
     * @param outRequestNo      业务申请单号
     * @param organizationType  主体类型
     * @param merchantShortname 商户简称
     */
    public SubMerchantApplyCommand(String outRequestNo, String organizationType, String merchantShortname) {
        this(outRequestNo, organizationType, merchantShortname, Collections.emptyMap());
    }

    /**
     * 读取渠道扩展参数。
     *
     * @param key 键
     * @return 值
     */
    public String param(String key) {
        return channelParams == null ? null : channelParams.get(key);
    }
}
