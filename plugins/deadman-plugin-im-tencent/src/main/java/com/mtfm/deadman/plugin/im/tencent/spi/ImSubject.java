package com.mtfm.deadman.plugin.im.tencent.spi;

/**
 * IM 抽象用户主体，与具体用户表解耦。
 *
 * @param realmId   用户域标识，如 client、admin
 * @param subjectId 域内稳定主键，如 userCode
 */
public record ImSubject(String realmId, String subjectId) {
}
