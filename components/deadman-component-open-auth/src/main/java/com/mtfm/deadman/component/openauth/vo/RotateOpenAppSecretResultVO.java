package com.mtfm.deadman.component.openauth.vo;

/**
 * 轮换开放应用密钥结果。
 *
 * @param appId        AppId
 * @param clientSecret 新密钥明文（仅展示一次）
 */
public record RotateOpenAppSecretResultVO(String appId, String clientSecret) {
}
