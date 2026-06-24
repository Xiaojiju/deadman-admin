package com.mtfm.deadman.plugin.logistics.constant;

/**
 * 物流模块 Spring Cache 缓存名称。
 */
public final class LogisticsCacheNames {

    /** 快递轨迹查询结果缓存 */
    public static final String TRACK_QUERY = "logisticsTrackQuery";

    /** 快递公司智能识别结果缓存 */
    public static final String CARRIER_DETECT = "logisticsCarrierDetect";

    private LogisticsCacheNames() {
    }
}
