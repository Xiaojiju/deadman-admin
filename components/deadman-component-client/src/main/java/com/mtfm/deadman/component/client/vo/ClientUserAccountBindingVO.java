package com.mtfm.deadman.component.client.vo;

/**
 * 用户端账号绑定信息。
 *
 * @param accountType       账号类型
 * @param accountIdentifier 账号标识
 * @param oauthProvider     OAuth 提供商
 * @param verified          是否已验证
 * @param status            账号状态
 */
public record ClientUserAccountBindingVO(
        String accountType, String accountIdentifier, String oauthProvider, Integer verified, Integer status) {
}
