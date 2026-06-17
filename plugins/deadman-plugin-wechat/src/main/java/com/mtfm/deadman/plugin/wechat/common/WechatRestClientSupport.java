package com.mtfm.deadman.plugin.wechat.common;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 微信开放平台 HTTP 调用与 JSON 解析公共支持。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatRestClientSupport {

    private final JsonMapper jsonMapper;
    private final RestClient restClient = RestClient.create();

    /**
     * 发起 GET 请求并解析 JSON 响应。
     *
     * @param url 请求地址
     * @return JSON 节点
     */
    public JsonNode getForJson(String url) {
        String rawBody = restClient.get().uri(url).retrieve().body(String.class);
        return parseJsonBody(rawBody);
    }

    /**
     * 发起 POST 请求并解析 JSON 响应。
     *
     * @param url         请求地址
     * @param requestBody 请求体
     * @return JSON 节点
     */
    public JsonNode postForJson(String url, Object requestBody) {
        String rawBody = restClient.post().uri(url).body(requestBody).retrieve().body(String.class);
        return parseJsonBody(rawBody);
    }

    /**
     * 解析微信 API 响应体。微信部分接口以 text/plain 返回 JSON，不能直接反序列化为 JsonNode。
     *
     * @param rawBody 原始响应字符串
     * @return 解析后的 JSON 节点
     */
    public JsonNode parseJsonBody(String rawBody) {
        if (!StringUtils.hasText(rawBody)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 API 响应为空");
        }
        try {
            return jsonMapper.readTree(rawBody);
        } catch (Exception ex) {
            log.warn("微信 API 响应解析失败: {}", rawBody, ex);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 API 响应格式错误");
        }
    }

    /**
     * 检查微信 API 响应是否成功。
     *
     * @param body           微信 API 响应体
     * @param defaultMessage 默认错误消息
     */
    public void assertWechatSuccess(JsonNode body, String defaultMessage) {
        if (body == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, defaultMessage);
        }
        if (body.has("errcode") && body.get("errcode").asInt() != 0) {
            String message = body.has("errmsg") ? body.get("errmsg").asString() : defaultMessage;
            log.warn("微信 API 失败: {}", message);
            throw new BusinessException(ResultCode.BAD_REQUEST, message);
        }
    }

    /**
     * 获取 JSON 节点文本值。
     *
     * @param node  JSON 节点
     * @param field 字段名
     * @return 文本值，不存在时返回 null
     */
    public String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }
}
