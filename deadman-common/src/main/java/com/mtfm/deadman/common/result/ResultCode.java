package com.mtfm.deadman.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 跨模块共用的响应码。账号、令牌、手机号与第三方绑定同时被多个模块使用，因此留在 common。
 * 只属于某一插件、扩展或 system 的业务码放在对应模块。
 */
@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未认证"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源冲突"),
    INTERNAL_ERROR(50000, "系统内部错误"),

    USER_NOT_FOUND(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已禁用"),
    ACCOUNT_EXISTS(10003, "账号已存在"),
    ACCOUNT_NOT_FOUND(10004, "账号不存在"),
    PASSWORD_MISMATCH(10005, "用户名或密码错误"),
    PASSWORD_NOT_SET(10006, "未设置密码"),
    USER_SUPER_ADMIN_PROTECTED(10007, "超级管理员用户不允许删除或停用"),
    TOKEN_INVALID(10008, "令牌无效或已过期"),
    TOKEN_REUSE_DETECTED(10009, "检测到令牌异常重用，请重新登录"),

    PHONE_EXISTS(12021, "手机号已被其他用户绑定"),

    OAUTH_ALREADY_BOUND(12031, "该第三方账号已被其他用户绑定");

    private final int code;
    private final String message;

    /**
     * 国际化文案键，对应各模块资源包中的 {@code result.<枚举名>}。
     *
     * @return 文案键
     */
    public String messageKey() {
        return "result." + name();
    }
}
