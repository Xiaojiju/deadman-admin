package com.mtfm.deadman.core.component;

import java.util.Collections;
import java.util.Map;

/**
 * 已装配组件描述符。
 * <p>
 * 约定：{@code components/} 下的模块在自动配置中通过 {@link DeadmanComponentContributor}
 * 或声明 {@code @Bean DeadmanComponentDescriptor} 完成注册，非强制校验。
 *
 * @param code        组件唯一编码，如 client
 * @param name        展示名称
 * @param description 组件说明
 * @param apiPrefix   API 路径前缀，如 /client/api
 * @param order       前端展示排序，升序
 * @param uiHints     前端扩展提示（键值对，可选）
 */
public record DeadmanComponentDescriptor(
        String code,
        String name,
        String description,
        String apiPrefix,
        int order,
        Map<String, Object> uiHints) {

    /**
     * 构造组件描述符，uiHints 为空时使用空 Map。
     *
     * @param code        组件编码
     * @param name        展示名称
     * @param description 说明
     * @param apiPrefix   API 前缀
     * @param order       排序
     */
    public DeadmanComponentDescriptor(String code, String name, String description, String apiPrefix, int order) {
        this(code, name, description, apiPrefix, order, Collections.emptyMap());
    }

    /**
     * 返回不可变的 uiHints 视图。
     *
     * @return UI 提示
     */
    @Override
    public Map<String, Object> uiHints() {
        return uiHints == null ? Map.of() : Collections.unmodifiableMap(uiHints);
    }
}
