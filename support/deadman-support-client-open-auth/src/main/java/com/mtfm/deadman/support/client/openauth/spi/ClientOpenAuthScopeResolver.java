package com.mtfm.deadman.support.client.openauth.spi;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.openauth.entity.OpenApp;
import com.mtfm.deadman.component.openauth.spi.OpenAuthScope;
import com.mtfm.deadman.component.openauth.spi.OpenAuthScopeResolver;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubject;
import com.mtfm.deadman.component.openauth.util.OpenAuthTextSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端开放授权 scope 解析器。
 * <p>
 * 首期返回应用配置的 default_scopes；后续可在此接入 RBAC、dataScope 等裁剪逻辑。
 */
@Component
public class ClientOpenAuthScopeResolver implements OpenAuthScopeResolver {

    /**
     * 用户端域标识。
     *
     * @return client
     */
    @Override
    public String realmId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    /**
     * 解析用户在指定应用下的授权范围。
     *
     * @param subject 当前用户
     * @param app     开放应用
     * @return 授权范围
     */
    @Override
    public OpenAuthScope resolve(OpenAuthSubject subject, OpenApp app) {
        List<String> permissions = OpenAuthTextSupport.splitCommaValues(app.getDefaultScopes());
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("subjectId", subject.subjectId());
        extensions.put("subjectCode", subject.subjectCode());
        return new OpenAuthScope(permissions, extensions);
    }
}
