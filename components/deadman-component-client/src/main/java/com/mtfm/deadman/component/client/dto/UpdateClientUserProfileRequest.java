package com.mtfm.deadman.component.client.dto;

import jakarta.validation.constraints.Size;

/**
 * 用户端更新当前用户资料请求（未传字段表示不修改）。
 *
 * @param nickname 昵称；传 null 表示不修改
 * @param avatar 头像 URL；传 null 表示不修改，传空串可清空头像
 */
public record UpdateClientUserProfileRequest(
    @Size(max = 64, message = "昵称长度不能超过 64") String nickname,
    @Size(max = 512, message = "头像地址长度不能超过 512") String avatar) {
}
