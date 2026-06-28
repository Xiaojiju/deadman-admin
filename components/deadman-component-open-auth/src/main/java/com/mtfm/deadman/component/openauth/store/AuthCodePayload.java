package com.mtfm.deadman.component.openauth.store;

import com.mtfm.deadman.component.openauth.spi.OpenAuthScope;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubject;

import java.util.List;
import java.util.Map;

/**
 * 授权码存储载荷。
 *
 * @param appId       开放应用 AppId
 * @param realm       用户域
 * @param subject     授权主体
 * @param permissions 授权 scope
 * @param extensions  扩展信息
 */
public record AuthCodePayload(
        String appId,
        String realm,
        OpenAuthSubject subject,
        List<String> permissions,
        Map<String, Object> extensions) {

    /**
     * 从 scope 构造载荷。
     *
     * @param appId   应用 AppId
     * @param realm   用户域
     * @param subject 授权主体
     * @param scope   授权范围
     * @return 载荷
     */
    public static AuthCodePayload of(String appId, String realm, OpenAuthSubject subject, OpenAuthScope scope) {
        return new AuthCodePayload(appId, realm, subject, scope.permissions(), scope.extensions());
    }
}
