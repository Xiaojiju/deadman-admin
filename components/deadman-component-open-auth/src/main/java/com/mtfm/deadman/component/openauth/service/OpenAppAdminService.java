package com.mtfm.deadman.component.openauth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.openauth.constant.OpenAuthConstants;
import com.mtfm.deadman.component.openauth.dto.CreateOpenAppRequest;
import com.mtfm.deadman.component.openauth.dto.UpdateOpenAppRequest;
import com.mtfm.deadman.component.openauth.entity.OpenApp;
import com.mtfm.deadman.component.openauth.mapper.OpenAppMapper;
import com.mtfm.deadman.component.openauth.util.OpenAuthCredentialGenerator;
import com.mtfm.deadman.component.openauth.util.OpenAuthTextSupport;
import com.mtfm.deadman.component.openauth.vo.CreateOpenAppResultVO;
import com.mtfm.deadman.component.openauth.vo.OpenAppSummaryVO;
import com.mtfm.deadman.component.openauth.vo.RotateOpenAppSecretResultVO;
import com.mtfm.deadman.core.password.PasswordEncoderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 开放应用管理服务。
 */
@Service
@RequiredArgsConstructor
public class OpenAppAdminService extends ServiceImpl<OpenAppMapper, OpenApp> {

    private static final int DEFAULT_CODE_TTL_SEC = 300;
    private static final int DEFAULT_TOKEN_TTL_SEC = 3600;

    private final PasswordEncoderRegistry passwordEncoderRegistry;

    /**
     * 查询全部开放应用。
     *
     * @return 应用列表
     */
    public List<OpenAppSummaryVO> listApps() {
        return list(new LambdaQueryWrapper<OpenApp>().orderByDesc(OpenApp::getCreateTime)).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * 创建开放应用并返回一次性 client_secret。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public CreateOpenAppResultVO createApp(CreateOpenAppRequest request) {
        String clientSecret = OpenAuthCredentialGenerator.generateClientSecret();
        PasswordEncoderRegistry.EncodedPassword encoded = encodeSecret(clientSecret);
        OpenApp app = OpenApp.builder()
                .appId(OpenAuthCredentialGenerator.generateAppId())
                .appName(request.appName().trim())
                .description(trimToNull(request.description()))
                .appSecretHash(encoded.hash())
                .secretEncoderId(encoded.encoderId())
                .status(OpenAuthConstants.APP_STATUS_ENABLED)
                .allowedRealms(OpenAuthTextSupport.joinCommaValues(normalizeList(request.allowedRealms())))
                .defaultScopes(OpenAuthTextSupport.joinCommaValues(normalizeList(request.defaultScopes())))
                .codeTtlSec(resolvePositive(request.codeTtlSec(), DEFAULT_CODE_TTL_SEC))
                .tokenTtlSec(resolvePositive(request.tokenTtlSec(), DEFAULT_TOKEN_TTL_SEC))
                .build();
        save(app);
        return new CreateOpenAppResultVO(
                app.getAppId(),
                app.getAppName(),
                clientSecret,
                OpenAuthTextSupport.splitCommaValues(app.getAllowedRealms()),
                OpenAuthTextSupport.splitCommaValues(app.getDefaultScopes()));
    }

    /**
     * 更新开放应用。
     *
     * @param id      主键
     * @param request 更新请求
     * @return 更新后摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public OpenAppSummaryVO updateApp(Long id, UpdateOpenAppRequest request) {
        OpenApp app = requireApp(id);
        app.setAppName(request.appName().trim());
        app.setDescription(trimToNull(request.description()));
        if (request.status() != null) {
            app.setStatus(request.status());
        }
        if (request.allowedRealms() != null && !request.allowedRealms().isEmpty()) {
            app.setAllowedRealms(OpenAuthTextSupport.joinCommaValues(normalizeList(request.allowedRealms())));
        }
        if (request.defaultScopes() != null) {
            app.setDefaultScopes(OpenAuthTextSupport.joinCommaValues(normalizeList(request.defaultScopes())));
        }
        if (request.codeTtlSec() != null) {
            app.setCodeTtlSec(resolvePositive(request.codeTtlSec(), DEFAULT_CODE_TTL_SEC));
        }
        if (request.tokenTtlSec() != null) {
            app.setTokenTtlSec(resolvePositive(request.tokenTtlSec(), DEFAULT_TOKEN_TTL_SEC));
        }
        updateById(app);
        return toSummary(app);
    }

    /**
     * 轮换开放应用密钥。
     *
     * @param id 主键
     * @return 新密钥（仅展示一次）
     */
    @Transactional(rollbackFor = Exception.class)
    public RotateOpenAppSecretResultVO rotateSecret(Long id) {
        OpenApp app = requireApp(id);
        String clientSecret = OpenAuthCredentialGenerator.generateClientSecret();
        PasswordEncoderRegistry.EncodedPassword encoded = encodeSecret(clientSecret);
        app.setAppSecretHash(encoded.hash());
        app.setSecretEncoderId(encoded.encoderId());
        updateById(app);
        return new RotateOpenAppSecretResultVO(app.getAppId(), clientSecret);
    }

    /**
     * 删除开放应用。
     *
     * @param id 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApp(Long id) {
        requireApp(id);
        removeById(id);
    }

    /**
     * 按 AppId 查询启用中的应用。
     *
     * @param appId AppId
     * @return 应用实体
     */
    public OpenApp requireEnabledByAppId(String appId) {
        OpenApp app = getOne(new LambdaQueryWrapper<OpenApp>().eq(OpenApp::getAppId, appId.trim()));
        if (app == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开放应用不存在");
        }
        if (!Integer.valueOf(OpenAuthConstants.APP_STATUS_ENABLED).equals(app.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开放应用已禁用");
        }
        return app;
    }

    /**
     * 校验 client_secret。
     *
     * @param app           应用实体
     * @param clientSecret  密钥明文
     * @return 是否匹配
     */
    public boolean matchesSecret(OpenApp app, String clientSecret) {
        return passwordEncoderRegistry.matches(app.getSecretEncoderId(), clientSecret, app.getAppSecretHash());
    }

    private OpenApp requireApp(Long id) {
        OpenApp app = getById(id);
        if (app == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开放应用不存在");
        }
        return app;
    }

    private OpenAppSummaryVO toSummary(OpenApp app) {
        return new OpenAppSummaryVO(
                app.getId(),
                app.getAppId(),
                app.getAppName(),
                app.getStatus(),
                app.getDescription(),
                OpenAuthTextSupport.splitCommaValues(app.getAllowedRealms()),
                OpenAuthTextSupport.splitCommaValues(app.getDefaultScopes()),
                app.getCodeTtlSec(),
                app.getTokenTtlSec(),
                app.getCreateTime(),
                app.getUpdateTime());
    }

    private PasswordEncoderRegistry.EncodedPassword encodeSecret(String clientSecret) {
        return passwordEncoderRegistry.encodeWithRandomEncoder(clientSecret);
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(String::trim).filter(StringUtils::hasText).distinct().toList();
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static int resolvePositive(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}
