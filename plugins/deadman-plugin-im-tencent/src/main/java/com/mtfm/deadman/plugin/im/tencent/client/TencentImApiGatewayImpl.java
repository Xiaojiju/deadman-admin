package com.mtfm.deadman.plugin.im.tencent.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 腾讯云 IM REST API 真实网关实现。
 */
@Slf4j
public class TencentImApiGatewayImpl implements TencentImApiGateway {

    private static final String ACCOUNT_IMPORT_PATH = "/v4/im_open_login_svc/account_import";

    private final ImTencentPluginProperties properties;
    private final TencentImUserSigGenerator userSigGenerator;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    /**
     * @param properties      插件配置
     * @param userSigGenerator UserSig 生成器
     * @param jsonMapper      JSON 映射器
     */
    public TencentImApiGatewayImpl(
            ImTencentPluginProperties properties, TencentImUserSigGenerator userSigGenerator, JsonMapper jsonMapper) {
        this.properties = properties;
        this.userSigGenerator = userSigGenerator;
        this.restClient = RestClient.create();
        this.jsonMapper = jsonMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateUserSig(String imUserId) {
        return userSigGenerator.generate(imUserId, properties.getUserSigExpireSeconds());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importAccount(String imUserId, String nickname, String avatarUrl) {
        properties.requireProductionConfig();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("UserID", imUserId);
        if (StringUtils.hasText(nickname)) {
            body.put("Nick", nickname);
        }
        if (StringUtils.hasText(avatarUrl)) {
            body.put("FaceUrl", avatarUrl);
        }
        JsonNode response = post(ACCOUNT_IMPORT_PATH, body);
        assertSuccess(response, "账号导入");
    }

    private JsonNode post(String path, Map<String, Object> body) {
        String adminUserSig =
                userSigGenerator.generate(properties.getAdminIdentifier(), properties.getUserSigExpireSeconds());
        int random = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        String url = properties.getApiBaseUrl()
                + path
                + "?sdkappid="
                + properties.getSdkAppId()
                + "&identifier="
                + properties.getAdminIdentifier()
                + "&usersig="
                + adminUserSig
                + "&random="
                + random
                + "&contenttype=json";
        try {
            String responseBody = restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonMapper.writeValueAsString(body))
                    .retrieve()
                    .body(String.class);
            return jsonMapper.readTree(responseBody);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.IM_ACCOUNT_SYNC_FAILED, "调用腾讯云 IM 失败：" + ex.getMessage());
        }
    }

    private void assertSuccess(JsonNode response, String action) {
        if (response == null) {
            throw new BusinessException(ResultCode.IM_ACCOUNT_SYNC_FAILED, action + "失败：响应为空");
        }
        String actionStatus = response.get("ActionStatus") == null
                ? null
                : response.get("ActionStatus").asString();
        int errorCode = response.get("ErrorCode") == null ? -1 : response.get("ErrorCode").asInt();
        if (!"OK".equalsIgnoreCase(actionStatus) || errorCode != 0) {
            String errorInfo = response.get("ErrorInfo") == null ? "unknown" : response.get("ErrorInfo").asString();
            log.warn("腾讯云 IM {} 失败：code={}, info={}", action, errorCode, errorInfo);
            throw new BusinessException(ResultCode.IM_ACCOUNT_SYNC_FAILED, action + "失败：" + errorInfo);
        }
    }
}
