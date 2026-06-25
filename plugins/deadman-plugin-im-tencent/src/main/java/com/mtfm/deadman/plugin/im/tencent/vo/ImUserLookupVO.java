package com.mtfm.deadman.plugin.im.tencent.vo;

/**
 * IM 用户映射查询结果。
 *
 * @param realmId   用户域标识
 * @param subjectId 域内主键
 * @param imUserId  腾讯云 UserID
 */
public record ImUserLookupVO(String realmId, String subjectId, String imUserId) {
}
