package com.mtfm.deadman.component.openauth.token;

import java.util.List;
import java.util.Map;

/**
 * 开放 access_token 签发上下文。
 *
 * @param appId         开放应用 AppId
 * @param realm         用户域
 * @param subjectType   主体类型
 * @param subjectId     主体主键
 * @param subjectCode   主体编码
 * @param permissions   授权 scope
 * @param extensions    扩展信息
 * @param ttlSeconds    有效期（秒）
 */
public record OpenTokenIssueContext(
        String appId,
        String realm,
        String subjectType,
        Long subjectId,
        String subjectCode,
        List<String> permissions,
        Map<String, Object> extensions,
        long ttlSeconds) {
}
