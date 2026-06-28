package com.mtfm.deadman.component.openauth.spi;

/**
 * 开放授权主体，表示已登录的业务用户。
 *
 * @param subjectType 主体类型，如 client_user
 * @param subjectId   主体主键
 * @param subjectCode 主体对外编码
 */
public record OpenAuthSubject(String subjectType, Long subjectId, String subjectCode) {
}
