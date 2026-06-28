package com.mtfm.deadman.component.openauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建开放应用请求。
 *
 * @param appName        应用名称
 * @param description    应用说明
 * @param allowedRealms  允许的用户域列表
 * @param defaultScopes  默认 scope 列表
 * @param codeTtlSec     auth_code 有效期（秒）
 * @param tokenTtlSec    access_token 有效期（秒）
 */
public record CreateOpenAppRequest(
        @NotBlank @Size(max = 64) String appName,
        @Size(max = 256) String description,
        @NotEmpty List<@NotBlank String> allowedRealms,
        List<@NotBlank String> defaultScopes,
        Integer codeTtlSec,
        Integer tokenTtlSec) {
}
