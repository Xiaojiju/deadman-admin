package com.mtfm.deadman.component.client.vo;

/**
 * 用户端当前用户资料。
 *
 * @param userCode 用户编码
 * @param username 主登录用户名
 * @param nickname 昵称
 * @param avatar   头像 URL
 * @param status   用户状态
 */
public record ClientUserProfileVO(String userCode, String username, String nickname, String avatar, Integer status) {
}
