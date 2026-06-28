package com.mtfm.deadman.component.openauth.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 开放应用摘要。
 *
 * @param id             主键
 * @param appId          AppId
 * @param appName        应用名称
 * @param status         状态
 * @param description    应用说明
 * @param allowedRealms  允许的用户域
 * @param defaultScopes  默认 scope
 * @param codeTtlSec     auth_code 有效期
 * @param tokenTtlSec    token 有效期
 * @param createTime     创建时间
 * @param updateTime     更新时间
 */
public record OpenAppSummaryVO(
        Long id,
        String appId,
        String appName,
        Integer status,
        String description,
        List<String> allowedRealms,
        List<String> defaultScopes,
        Integer codeTtlSec,
        Integer tokenTtlSec,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
