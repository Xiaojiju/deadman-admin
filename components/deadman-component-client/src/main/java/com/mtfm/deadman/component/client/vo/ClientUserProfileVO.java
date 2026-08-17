package com.mtfm.deadman.component.client.vo;

/**
 * 用户端当前用户资料。
 *
 * @param userCode     用户编码
 * @param username     主登录用户名
 * @param nickname     昵称
 * @param avatarFileId 头像文件 ID（client 模块不依赖文件插件，URL 由上层按需解析）
 * @param phone        绑定手机号（已脱敏）
 * @param status       用户状态
 */
public record ClientUserProfileVO(String userCode, String username, String nickname, Long avatarFileId, String phone,
    Integer status) {
}
