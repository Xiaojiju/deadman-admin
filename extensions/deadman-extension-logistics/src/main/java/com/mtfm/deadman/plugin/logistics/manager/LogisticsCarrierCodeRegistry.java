package com.mtfm.deadman.plugin.logistics.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierCodeContributor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 快递公司编码注册表：维护各 Provider 的统一编码与厂商编码双向映射。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsCarrierCodeRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final ObjectProvider<LogisticsCarrierCodeContributor> contributors;

    /** providerId -> (unifiedCode -> providerCode) */
    private final Map<String, Map<String, String>> unifiedToProvider = new ConcurrentHashMap<>();
    /** providerId -> (providerCode -> unifiedCode) */
    private final Map<String, Map<String, String>> providerToUnified = new ConcurrentHashMap<>();

    private volatile boolean contributorsLoaded;

    /**
     * 将平台统一编码转换为指定 Provider 的厂商编码。
     *
     * @param providerId   Provider 标识
     * @param unifiedCode  平台统一编码
     * @return 厂商编码
     */
    public String toProviderCode(String providerId, String unifiedCode) {
        String normalizedUnified = normalizeUnifiedCode(unifiedCode);
        String providerCode = lookupProviderCode(providerId, normalizedUnified);
        if (!StringUtils.hasText(providerCode)) {
            throw new BusinessException(
                    ResultCode.LOGISTICS_CARRIER_CODE_UNKNOWN,
                    "快递公司编码未注册或不支持当前渠道: " + normalizedUnified);
        }
        return providerCode;
    }

    /**
     * 将 Provider 返回的厂商编码转换为平台统一编码。
     *
     * @param providerId   Provider 标识
     * @param providerCode 厂商编码
     * @return 平台统一编码；若未注册映射则原样返回（大写）
     */
    public String toUnifiedCode(String providerId, String providerCode) {
        if (!StringUtils.hasText(providerCode)) {
            return providerCode;
        }
        String normalizedProvider = normalizeProviderCode(providerCode);
        String unified = lookupUnifiedCode(providerId, normalizedProvider);
        if (StringUtils.hasText(unified)) {
            return unified;
        }
        log.debug("Provider {} 返回未注册厂商编码 {}，原样透传", providerId, normalizedProvider);
        return normalizedProvider.toUpperCase(Locale.ROOT);
    }

    /**
     * 列出指定 Provider 已注册的平台统一编码（只读快照）。
     *
     * @param providerId Provider 标识
     * @return 统一编码列表
     */
    public List<String> listUnifiedCodes(String providerId) {
        Map<String, String> mapping = unifiedToProvider.get(normalizeProviderId(providerId));
        if (mapping == null || mapping.isEmpty()) {
            return List.of();
        }
        List<String> codes = new ArrayList<>(mapping.keySet());
        Collections.sort(codes);
        return Collections.unmodifiableList(codes);
    }

    /**
     * 直接注册编码映射贡献者（供测试或模块内显式注册使用）。
     *
     * @param contributor 编码映射贡献者
     */
    public void register(LogisticsCarrierCodeContributor contributor) {
        registerContributor(contributor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        if (contributorsLoaded) {
            return;
        }
        contributorsLoaded = true;
        if (contributors != null) {
            contributors.forEach(this::registerContributor);
        }
        log.info(
                "快递公司编码映射注册完成，共 {} 个 Provider：{}",
                unifiedToProvider.size(),
                unifiedToProvider.keySet());
    }

    private void registerContributor(LogisticsCarrierCodeContributor contributor) {
        if (contributor == null || !StringUtils.hasText(contributor.providerId())) {
            return;
        }
        String providerId = normalizeProviderId(contributor.providerId());
        Map<String, String> forward = new HashMap<>();
        Map<String, String> reverse = new HashMap<>();
        Map<String, String> contributed = contributor.contribute();
        if (contributed != null) {
            contributed.forEach((unified, providerCode) -> {
                if (!StringUtils.hasText(unified) || !StringUtils.hasText(providerCode)) {
                    return;
                }
                String normalizedUnified = normalizeUnifiedCode(unified);
                String normalizedProvider = normalizeProviderCode(providerCode);
                forward.put(normalizedUnified, normalizedProvider);
                reverse.put(normalizedProvider, normalizedUnified);
            });
        }
        unifiedToProvider.put(providerId, Map.copyOf(forward));
        providerToUnified.put(providerId, Map.copyOf(reverse));
        log.info("物流 Provider {} 注册快递公司编码映射 {} 条", providerId, forward.size());
    }

    private String lookupProviderCode(String providerId, String unifiedCode) {
        Map<String, String> mapping = unifiedToProvider.get(normalizeProviderId(providerId));
        return mapping != null ? mapping.get(unifiedCode) : null;
    }

    private String lookupUnifiedCode(String providerId, String providerCode) {
        Map<String, String> mapping = providerToUnified.get(normalizeProviderId(providerId));
        return mapping != null ? mapping.get(providerCode) : null;
    }

    private static String normalizeProviderId(String providerId) {
        return providerId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeUnifiedCode(String unifiedCode) {
        if (!StringUtils.hasText(unifiedCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "快递公司编码不能为空");
        }
        return unifiedCode.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeProviderCode(String providerCode) {
        return providerCode.trim().toLowerCase(Locale.ROOT);
    }
}
