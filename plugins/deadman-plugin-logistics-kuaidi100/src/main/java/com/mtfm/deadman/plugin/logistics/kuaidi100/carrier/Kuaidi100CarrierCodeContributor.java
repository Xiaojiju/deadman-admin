package com.mtfm.deadman.plugin.logistics.kuaidi100.carrier;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierCodeContributor;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers;

/**
 * 快递100 快递公司编码映射贡献者：平台统一编码 ↔ 快递100 {@code com} 字段。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100CarrierCodeContributor implements LogisticsCarrierCodeContributor {

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return Kuaidi100ProviderIds.KUAIDI100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> contribute() {
        return Map.ofEntries(
                Map.entry(LogisticsCarriers.YTO, "yuantong"),
                Map.entry(LogisticsCarriers.SF, "shunfeng"),
                Map.entry(LogisticsCarriers.STO, "shentong"),
                Map.entry(LogisticsCarriers.ZTO, "zhongtong"),
                Map.entry(LogisticsCarriers.YD, "yunda"),
                Map.entry(LogisticsCarriers.JD, "jd"),
                Map.entry(LogisticsCarriers.EMS, "ems"),
                Map.entry(LogisticsCarriers.JTSD, "jtexpress"),
                Map.entry(LogisticsCarriers.DBL, "debangkuaidi"),
                Map.entry(LogisticsCarriers.HTKY, "huitongkuaidi"),
                Map.entry(LogisticsCarriers.YZPY, "youzhengguonei"),
                Map.entry(LogisticsCarriers.YZBK, "youzhengbk"),
                Map.entry(LogisticsCarriers.UC, "youshuwuliu"),
                Map.entry(LogisticsCarriers.HHTT, "tiantian"),
                Map.entry(LogisticsCarriers.ZJS, "zhaijisong"));
    }
}
