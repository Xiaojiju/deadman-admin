package com.mtfm.deadman.component.openauth.vo;

/**
 * 授权码签发结果。
 *
 * @param code      授权码
 * @param expiresIn 有效期（秒）
 * @param appId     目标应用 AppId
 */
public record OpenAuthCodeVO(String code, int expiresIn, String appId) {
}
