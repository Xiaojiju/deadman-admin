package com.mtfm.deadman.support.client.file.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标在返回二进制或图片的接口上：出错时返回非 2xx，响应体仍是 Result JSON，业务码不变。
 * <p>用 uni.downloadFile 这类只看 HTTP 状态码的客户端，才不会把错误 JSON 当成文件保存下来。普通 JSON 接口不要标，
 * 它们保持 HTTP 200 加业务码。状态码映射见 {@link BinaryErrorStatusAdvice}。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BinaryErrorResponse {

    /**
     * 额外按「不存在」处理、返回 404 的业务码，例如组件自己的「还没有照片」。
     *
     * @return 业务码
     */
    int[] notFoundCodes() default {};
}
