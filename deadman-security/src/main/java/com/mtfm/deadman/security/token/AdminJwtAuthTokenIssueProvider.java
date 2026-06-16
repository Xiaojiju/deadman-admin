package com.mtfm.deadman.security.token;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.jwt.RealmJwtSupport;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 管理端（ADMIN）JWT 双令牌签发 Provider。
 */
@Component
public class AdminJwtAuthTokenIssueProvider extends AbstractJwtAuthTokenIssueProvider {

    private final UserService userService;

    /**
     * @param deadmanProperties       管理端配置
     * @param redisTemplateProvider   Redis 模板（可选）
     * @param userService             用户服务（刷新时加载用户）
     */
    public AdminJwtAuthTokenIssueProvider(
            DeadmanProperties deadmanProperties,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            UserService userService) {
        super(RealmJwtSupport.create(buildSettings(deadmanProperties), redisTemplateProvider));
        this.userService = userService;
    }

    @Override
    protected AuthTokenSubject loadSubjectForRefresh(Long userId) {
        UserBase userBase = userService.getById(userId);
        if (userBase == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return new AuthTokenSubject(userBase.getId(), userBase.getUserCode(), userBase.getNickname());
    }

    private static RealmJwtSettings buildSettings(DeadmanProperties deadmanProperties) {
        DeadmanProperties.Jwt jwt = deadmanProperties.getJwt();
        return RealmJwtSettingsFactory.create(
                AdminAuthConstants.JWT_REALM,
                jwt.getSecret(),
                jwt.getAccessExpirationMs(),
                jwt.getExpirationMs(),
                jwt.getRefreshExpirationMs(),
                deadmanProperties.getSecurity().isMultiSessionEnabled(),
                AdminAuthConstants.SESSION_KEY_PREFIX,
                AdminAuthConstants.REFRESH_KEY_PREFIX,
                AdminAuthConstants.REFRESH_USER_INDEX_PREFIX,
                jwt.isRefreshCookieSecure(),
                AdminAuthConstants.REFRESH_TOKEN_PATH,
                AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME,
                "/api/auth");
    }
}
