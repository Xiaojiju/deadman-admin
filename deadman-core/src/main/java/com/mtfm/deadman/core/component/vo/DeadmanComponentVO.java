package com.mtfm.deadman.core.component.vo;

/**
 * 已装配组件对外展示信息。
 *
 * @param code        组件唯一编码
 * @param name        展示名称
 * @param description 组件说明
 * @param apiPrefix   API 路径前缀
 * @param order       前端展示排序
 * @param uiHints     前端扩展提示
 */
public record DeadmanComponentVO(
        String code,
        String name,
        String description,
        String apiPrefix,
        int order,
        DeadmanComponentUiHints uiHints) {
}
