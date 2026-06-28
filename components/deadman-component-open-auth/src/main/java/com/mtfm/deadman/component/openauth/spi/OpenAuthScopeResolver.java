package com.mtfm.deadman.component.openauth.spi;

import com.mtfm.deadman.component.openauth.entity.OpenApp;

/**
 * 解析用户在指定开放应用下的授权范围。
 */
public interface OpenAuthScopeResolver {

    /**
     * 支持的 realm。
     *
     * @return 域标识
     */
    String realmId();

    /**
     * 解析授权范围。
     *
     * @param subject 当前用户主体
     * @param app     目标开放应用
     * @return 授权范围
     */
    OpenAuthScope resolve(OpenAuthSubject subject, OpenApp app);
}
