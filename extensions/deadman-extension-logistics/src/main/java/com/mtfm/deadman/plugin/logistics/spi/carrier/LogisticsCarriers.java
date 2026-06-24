package com.mtfm.deadman.plugin.logistics.spi.carrier;

/**
 * 平台统一快递公司编码常量。
 * <p>
 * 业务层与 REST API 应使用本类定义的编码；各渠道插件通过 {@link LogisticsCarrierCodeContributor}
 * 将其映射为厂商私有编码（如快递100 的 {@code yuantong}）。
 */
public final class LogisticsCarriers {

    /** 圆通速递 */
    public static final String YTO = "YTO";
    /** 顺丰速运 */
    public static final String SF = "SF";
    /** 申通快递 */
    public static final String STO = "STO";
    /** 中通快递 */
    public static final String ZTO = "ZTO";
    /** 韵达速递 */
    public static final String YD = "YD";
    /** 京东物流 */
    public static final String JD = "JD";
    /** 中国邮政 EMS */
    public static final String EMS = "EMS";
    /** 极兔速递 */
    public static final String JTSD = "JTSD";
    /** 德邦快递 */
    public static final String DBL = "DBL";
    /** 百世快递 */
    public static final String HTKY = "HTKY";
    /** 邮政快递包裹 */
    public static final String YZPY = "YZPY";
    /** 邮政标准快递 */
    public static final String YZBK = "YZBK";
    /** 优速快递 */
    public static final String UC = "UC";
    /** 天天快递 */
    public static final String HHTT = "HHTT";
    /** 宅急送 */
    public static final String ZJS = "ZJS";

    private LogisticsCarriers() {
    }
}
