package com.mtfm.deadman.common.constants;

/**
 * 密码编码器标识，持久化在 user_password.encoder_id。
 */
public final class PasswordEncoderIds {

    public static final String BCRYPT_10 = "bcrypt-10";
    public static final String BCRYPT_12 = "bcrypt-12";
    public static final String PBKDF2 = "pbkdf2";

    private PasswordEncoderIds() {
    }
}
