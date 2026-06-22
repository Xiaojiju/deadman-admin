package com.mtfm.deadman.core.component.vo;

import java.util.List;

/**
 * 组件前端扩展提示。
 *
 * @param authBasePath 认证 API 基础路径
 * @param features     组件能力特性列表
 */
public record DeadmanComponentUiHints(String authBasePath, List<String> features) {
}
