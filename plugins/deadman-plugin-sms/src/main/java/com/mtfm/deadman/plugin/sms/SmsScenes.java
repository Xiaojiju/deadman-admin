package com.mtfm.deadman.plugin.sms;

/**
 * 常用验证码场景。业务侧也可以传入自己的场景名。
 */
public final class SmsScenes {

    /** 手机号登录 */
    public static final String LOGIN = "LOGIN";

    /** 更换手机号，验证的是新号码 */
    public static final String CHANGE_PHONE = "CHANGE_PHONE";

    private SmsScenes() {}
}
