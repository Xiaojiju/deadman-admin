package com.mtfm.deadman.component.openauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * OAuth Token 兑换请求（兼容 snake_case 字段名）。
 *
 * @param grantType    授权类型，固定 authorization_code
 * @param clientId     开放应用 AppId
 * @param clientSecret 应用密钥
 * @param code         授权码
 */
public record OpenOAuthTokenRequest(
        @NotBlank @JsonProperty("grant_type") String grantType,
        @NotBlank @JsonProperty("client_id") String clientId,
        @NotBlank @JsonProperty("client_secret") String clientSecret,
        @NotBlank String code) {
}
