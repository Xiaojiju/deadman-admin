package com.mtfm.deadman.core.component;

import com.mtfm.deadman.core.component.vo.DeadmanComponentUiHints;

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
 * @param uiHints     前端扩展提示（可选）
 */
public record DeadmanComponentDescriptor(
        String code,
        String name,
        String description,
        String apiPrefix,
        int order,
        DeadmanComponentUiHints uiHints) {

    /**
     * 构造组件描述符，uiHints 为空时使用 null。
     *
     * @param code        组件编码
     * @param name        展示名称
     * @param description 说明
     * @param apiPrefix   API 前缀
     * @param order       排序
     */
    public DeadmanComponentDescriptor(String code, String name, String description, String apiPrefix, int order) {
        this(code, name, description, apiPrefix, order, null);
    }
}
