package com.mtfm.deadman.common.validation;

/**
 * 中国大陆手机号校验规则（用于 {@link jakarta.validation.constraints.Pattern}）。
 */
public final class PhoneNumber {

    public static final String PATTERN = "^1[3-9]\\d{9}$";
    public static final String MESSAGE = "手机号格式不正确";

    private PhoneNumber() {
    }
}
