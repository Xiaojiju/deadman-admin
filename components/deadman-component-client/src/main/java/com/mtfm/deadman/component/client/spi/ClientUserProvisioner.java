package com.mtfm.deadman.component.client.spi;

import com.mtfm.deadman.component.client.entity.ClientUserBase;

/**
 * 用户端用户注入 SPI，供 OAuth 等插件在首次登录时创建或关联用户。
 */
public interface ClientUserProvisioner {

    /**
     * 按 OAuth 信息创建或关联用户。
     *
     * @param request 注入请求
     * @return 用户基础信息
     */
    ClientUserBase provisionOAuthUser(ClientUserProvisionRequest request);

    /**
     * OAuth 用户注入请求。
     *
     * @param provider OAuth 提供商
     * @param subject OAuth 用户唯一标识
     * @param accountIdentifier 账号标识，通常与 subject 相同
     * @param nickname 昵称
     * @param avatarFileId 头像文件 ID；微信等仅提供外链 CDN URL 时须传 null，禁止把外链当作 fileId 写入
     */
    record ClientUserProvisionRequest(String provider, String subject, String accountIdentifier, String nickname,
        Long avatarFileId) {
    }
}
