package com.mtfm.deadman.plugin.im.tencent.spi;

/**
 * 业务侧提供的 IM 资料快照，不含腾讯云 UserID。
 *
 * @param nickname  昵称
 * @param avatarUrl 头像 URL
 * @param enabled   是否允许使用 IM
 */
public record ImUserProfileSource(String nickname, String avatarUrl, boolean enabled) {
}
