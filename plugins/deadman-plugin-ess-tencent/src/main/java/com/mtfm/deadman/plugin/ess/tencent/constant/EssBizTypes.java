package com.mtfm.deadman.plugin.ess.tencent.constant;

/**
 * 腾讯电子签业务类型约定（写入 UserData 的 bizType 段）。
 * <p>
 * 业务模块发起合同时应使用 {@link com.mtfm.deadman.plugin.ess.tencent.support.EssUserDataSupport#encode}，
 * 监听 {@link com.mtfm.deadman.plugin.ess.tencent.event.EssFlowCallbackEvent} 时按 bizType 过滤。
 */
public final class EssBizTypes {

    /** 工程信息线上签约服务费订单 */
    public static final String SIGN_ORDER = "SIGN_ORDER";

    /** 平台保险签约订单 */
    public static final String INSURANCE_SIGN_ORDER = "INSURANCE_SIGN_ORDER";

    private EssBizTypes() {
    }
}
