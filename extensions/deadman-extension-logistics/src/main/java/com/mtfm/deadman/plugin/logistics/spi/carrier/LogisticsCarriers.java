package com.mtfm.deadman.plugin.logistics.spi.carrier;

import java.util.Locale;
import java.util.Map;

import org.springframework.util.StringUtils;

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

    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry(YTO, "圆通速递"),
            Map.entry(SF, "顺丰速运"),
            Map.entry(STO, "申通快递"),
            Map.entry(ZTO, "中通快递"),
            Map.entry(YD, "韵达速递"),
            Map.entry(JD, "京东物流"),
            Map.entry(EMS, "中国邮政 EMS"),
            Map.entry(JTSD, "极兔速递"),
            Map.entry(DBL, "德邦快递"),
            Map.entry(HTKY, "百世快递"),
            Map.entry(YZPY, "邮政快递包裹"),
            Map.entry(YZBK, "邮政标准快递"),
            Map.entry(UC, "优速快递"),
            Map.entry(HHTT, "天天快递"),
            Map.entry(ZJS, "宅急送"));

    private LogisticsCarriers() {
    }

    /**
     * 按平台统一编码解析展示名称。
     *
     * @param unifiedCode 平台统一编码
     * @return 中文名称；未知编码时返回规范化后的编码本身
     */
    public static String displayName(String unifiedCode) {
        if (!StringUtils.hasText(unifiedCode)) {
            return unifiedCode;
        }
        String normalized = unifiedCode.trim().toUpperCase(Locale.ROOT);
        return DISPLAY_NAMES.getOrDefault(normalized, normalized);
    }
}
