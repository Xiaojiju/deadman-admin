package com.mtfm.deadman.plugin.sms;

/**
 * 短信验证码业务码。文案在本插件资源包。
 */
public final class SmsErrorCodes {

    /** 验证码错误或过期 */
    public static final int CODE_INVALID = 14611;

    /** 发送过于频繁 */
    public static final int TOO_FREQUENT = 14612;

    private SmsErrorCodes() {}
}
