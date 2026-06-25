package com.mtfm.deadman.plugin.im.tencent.client;

/**
 * 腾讯云 IM API 网关。
 */
public interface TencentImApiGateway {

    /**
     * 为指定 UserID 生成 UserSig。
     *
     * @param imUserId IM UserID
     * @return UserSig
     */
    String generateUserSig(String imUserId);

    /**
     * 导入或更新 IM 账号资料。
     *
     * @param imUserId  IM UserID
     * @param nickname  昵称
     * @param avatarUrl 头像 URL
     */
    void importAccount(String imUserId, String nickname, String avatarUrl);
}
