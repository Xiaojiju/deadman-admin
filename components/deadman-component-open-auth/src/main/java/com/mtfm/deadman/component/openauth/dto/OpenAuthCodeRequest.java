package com.mtfm.deadman.component.openauth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 申请开放授权码请求。
 *
 * @param appId 开放应用 AppId
 */
public record OpenAuthCodeRequest(@NotBlank String appId) {
}
