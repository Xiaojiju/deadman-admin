package com.mtfm.deadman.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户账号类型。
 */
@Getter
@RequiredArgsConstructor
public enum AccountType {

    USERNAME("USERNAME", "用户名密码登录"),
    PHONE("PHONE", "手机号验证码登录"),
    OAUTH("OAUTH", "第三方 OAuth2 登录");

    private final String code;
    private final String description;

    public static AccountType fromCode(String code) {
        for (AccountType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知账号类型: " + code);
    }
}
