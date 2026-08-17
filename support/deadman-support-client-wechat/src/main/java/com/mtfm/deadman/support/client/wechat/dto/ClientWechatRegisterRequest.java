package com.mtfm.deadman.support.client.wechat.dto;

import com.mtfm.deadman.common.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户端微信 OAuth 绑定注册请求（新用户注册并绑定 openid）。
 * <p>
 * 通过 {@code mode} 分发：
 * <ul>
 *   <li>{@code PASSWORD}（缺省）：须提供 username + password</li>
 *   <li>{@code PROFILE}：仅完善资料（nickname + phone），服务端生成账密</li>
 * </ul>
 *
 * @param bindToken    微信登录返回的临时绑定令牌
 * @param mode         注册模式：PASSWORD / PROFILE；空则按 PASSWORD
 * @param username     登录用户名（PASSWORD 必填）
 * @param password     密码（PASSWORD 必填）
 * @param nickname     昵称；PROFILE 必填，PASSWORD 可空
 * @param avatarFileId 头像文件 ID，可为空；微信 CDN 外链不可作为 fileId，仅平台上传文件 ID 可传
 * @param phone        手机号；PROFILE 必填；PASSWORD 可空（后续 getPhoneNumber 绑定）
 * @param inviteCode   推广码，可为空
 */
public record ClientWechatRegisterRequest(
        @NotBlank(message = "绑定令牌不能为空") String bindToken,
        @Size(max = 16) String mode,
        @Size(min = 3, max = 64) String username,
        @Size(min = 8, max = 64) String password,
        @Size(max = 64) String nickname,
        Long avatarFileId,
        @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phone,
        @Size(max = 32) String inviteCode) {

    /** 账密建号（兼容旧客户端） */
    public static final String MODE_PASSWORD = "PASSWORD";

    /** 仅完善资料，服务端生成账密 */
    public static final String MODE_PROFILE = "PROFILE";

    /**
     * 解析注册模式；缺省或非法值回退为 PASSWORD（兼容旧请求）。
     */
    public String resolvedMode() {
        if (mode == null || mode.isBlank()) {
            return MODE_PASSWORD;
        }
        String normalized = mode.trim().toUpperCase();
        if (MODE_PROFILE.equals(normalized)) {
            return MODE_PROFILE;
        }
        return MODE_PASSWORD;
    }

    public boolean isProfileMode() {
        return MODE_PROFILE.equals(resolvedMode());
    }
}
