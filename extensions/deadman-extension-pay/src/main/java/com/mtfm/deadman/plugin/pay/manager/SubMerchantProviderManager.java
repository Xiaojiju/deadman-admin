package com.mtfm.deadman.plugin.pay.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantProvider;

/**
 * 二级商户 Provider 注册表。
 */
@Component
public class SubMerchantProviderManager {

    private final List<SubMerchantProvider> providers;

    /**
     * @param providers Spring 注入的全部 SubMerchantProvider
     */
    public SubMerchantProviderManager(List<SubMerchantProvider> providers) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
    }

    /**
     * 按标识解析 Provider；空标识时取第一个。
     *
     * @param providerId 标识
     * @return Provider
     */
    public SubMerchantProvider require(String providerId) {
        if (providers.isEmpty()) {
            throw new BusinessException(ResultCode.PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND, "未注册二级商户 Provider");
        }
        if (!StringUtils.hasText(providerId)) {
            return providers.getFirst();
        }
        return providers.stream()
                .filter(p -> p.supports(providerId.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ResultCode.PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND, "未知二级商户 Provider: " + providerId));
    }

    /**
     * @return 已注册标识列表
     */
    public List<String> listProviderIds() {
        return providers.stream().map(SubMerchantProvider::providerId).toList();
    }
}
