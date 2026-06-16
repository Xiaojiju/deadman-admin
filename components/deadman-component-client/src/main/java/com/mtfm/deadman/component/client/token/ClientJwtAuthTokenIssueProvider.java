package com.mtfm.deadman.component.client.token;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.config.ClientComponentProperties;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.security.jwt.RealmJwtSupport;
import com.mtfm.deadman.security.token.AbstractJwtAuthTokenIssueProvider;
import com.mtfm.deadman.security.token.AuthTokenSubject;
import com.mtfm.deadman.security.token.RealmJwtSettings;
import com.mtfm.deadman.security.token.RealmJwtSettingsFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 用户端（CLIENT）JWT 双令牌签发 Provider，复用 security 模块 JWT 引擎。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClientJwtAuthTokenIssueProvider extends AbstractJwtAuthTokenIssueProvider {

    private final ClientUserService clientUserService;

    /**
     * @param clientComponentProperties 用户端组件配置
     * @param redisTemplateProvider     Redis 模板（可选）
     * @param clientUserService         用户端用户服务
     */
    public ClientJwtAuthTokenIssueProvider(
            ClientComponentProperties clientComponentProperties,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            ClientUserService clientUserService) {
        super(RealmJwtSupport.create(buildSettings(clientComponentProperties), redisTemplateProvider));
        this.clientUserService = clientUserService;
    }

    @Override
    protected AuthTokenSubject loadSubjectForRefresh(Long userId) {
        ClientUserBase userBase = clientUserService.getById(userId);
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return new AuthTokenSubject(userBase.getId(), userBase.getUserCode(), userBase.getNickname());
    }

    private static RealmJwtSettings buildSettings(ClientComponentProperties properties) {
        ClientComponentProperties.Jwt jwt = properties.getJwt();
        String authBase = properties.getAuth().getBasePath();
        return RealmJwtSettingsFactory.create(
                ClientAuthConstants.JWT_REALM,
                jwt.getSecret(),
                jwt.getAccessExpirationMs(),
                jwt.getExpirationMs(),
                jwt.getRefreshExpirationMs(),
                properties.getSecurity().isMultiSessionEnabled(),
                ClientAuthConstants.SESSION_KEY_PREFIX,
                ClientAuthConstants.REFRESH_KEY_PREFIX,
                ClientAuthConstants.REFRESH_USER_INDEX_PREFIX,
                jwt.isRefreshCookieSecure(),
                ClientAuthConstants.REFRESH_TOKEN_PATH,
                ClientAuthConstants.REFRESH_TOKEN_COOKIE_NAME,
                authBase);
    }
}
