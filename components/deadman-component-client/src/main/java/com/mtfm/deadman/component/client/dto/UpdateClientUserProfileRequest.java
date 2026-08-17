package com.mtfm.deadman.component.client.dto;

import com.mtfm.deadman.common.validation.PhoneNumber;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户端更新本人资料请求。
 *
 * @param nickname 昵称；传 null 表示不修改
 * @param avatarFileId 头像文件 ID；传 null 表示不修改
 * @param phone 手机号；传 null 表示不修改；传空串会报错
 */
public record UpdateClientUserProfileRequest(
    @Size(max = 64, message = "昵称长度不能超过 64") String nickname,
    Long avatarFileId,
    @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone) {
}
