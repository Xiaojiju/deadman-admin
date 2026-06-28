package com.mtfm.deadman.component.openauth.vo;

/**
 * 开放授权主体信息。
 *
 * @param realm       用户域
 * @param subjectType 主体类型
 * @param subjectId   主体主键
 * @param subjectCode 主体编码
 */
public record OpenAuthSubjectVO(String realm, String subjectType, Long subjectId, String subjectCode) {
}
