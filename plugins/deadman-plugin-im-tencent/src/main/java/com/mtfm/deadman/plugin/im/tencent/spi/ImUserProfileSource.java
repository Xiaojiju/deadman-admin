package com.mtfm.deadman.plugin.im.tencent.spi;

/**
 * 业务侧提供的 IM 资料快照，不含腾讯云 UserID。
 * <p>头像由调用方（同时依赖 file 与 im 的 Support）先换链再传入；
 * 插件本身不依赖文件服务。
 *
 * @param nickname     昵称
 * @param avatarUrl    头像公网绝对 URL（http/https）；不可用时为 null
 * @param avatarFileId 头像文件 ID（稳定比对键，无文件依赖语义）；无头像时为 null
 * @param enabled      是否允许使用 IM
 */
public record ImUserProfileSource(String nickname, String avatarUrl, Long avatarFileId, boolean enabled) {
}
