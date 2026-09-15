package com.mtfm.deadman.plugin.ess.tencent.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.ess.tencent.event.EssFlowCallbackEvent;
import com.mtfm.deadman.plugin.ess.tencent.event.EssOrgAuthCallbackEvent;
import com.mtfm.deadman.plugin.ess.tencent.support.EssUserDataSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 腾讯电子签回调解密、解析并发布 {@link EssFlowCallbackEvent}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EssCallbackDispatchService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final EssSignService essSignService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理公开回调：验签解密 → 解析公共字段 → 发布领域事件。
     *
     * @param payload          原始回调报文
     * @param contentSignature 请求头 Content-Signature
     */
    public void dispatch(String payload, String contentSignature) {
        String plain = essSignService.decryptCallbackPayload(payload, contentSignature);
        if (!StringUtils.hasText(plain)) {
            log.debug("电子签回调明文为空，忽略");
            return;
        }
        EssFlowCallbackEvent event = parseEvent(plain);
        if (isOrgAuthCallback(event.msgType())) {
            EssOrgAuthCallbackEvent orgEvent = parseOrgAuthEvent(plain, event);
            if (!StringUtils.hasText(orgEvent.proxyOrganizationOpenId())) {
                log.debug("电子签子客认证回调缺少 ProxyOrganizationOpenId，忽略: msgType={}", event.msgType());
                return;
            }
            log.info("发布电子签子客认证回调: orgOpenId={}, openSuccess={}",
                    orgEvent.proxyOrganizationOpenId(), orgEvent.openSuccess());
            eventPublisher.publishEvent(orgEvent);
            return;
        }
        if (!StringUtils.hasText(event.flowId())) {
            log.debug("电子签回调缺少 FlowId，忽略: msgType={}", event.msgType());
            return;
        }
        log.info(
                "发布电子签流程回调事件: msgType={}, flowId={}, flowStatus={}, bizType={}, bizRef={}",
                event.msgType(),
                event.flowId(),
                event.flowStatus(),
                event.bizType(),
                event.bizRef());
        eventPublisher.publishEvent(event);
    }

    /**
     * 将明文 JSON 解析为回调事件（兼容根节点字段或 MsgData 嵌套字符串）。
     *
     * @param plain 明文 JSON
     * @return 事件
     */
    EssFlowCallbackEvent parseEvent(String plain) {
        try {
            JsonNode root = JSON_MAPPER.readTree(plain);
            JsonNode dataNode = resolveDataNode(root);
            String msgType = textOrNull(root, "MsgType");
            if (!StringUtils.hasText(msgType)) {
                msgType = textOrNull(dataNode, "MsgType");
            }
            String flowId = firstText(dataNode, root, "FlowId");
            String flowStatus = firstText(dataNode, root, "FlowStatus");
            String userData = firstText(dataNode, root, "UserData");
            if (!StringUtils.hasText(userData)) {
                // 渠道版模板发起使用 CustomerData 透传业务关联
                userData = firstText(dataNode, root, "CustomerData");
            }
            return new EssFlowCallbackEvent(
                    msgType,
                    flowId,
                    flowStatus,
                    userData,
                    EssUserDataSupport.parseBizType(userData),
                    EssUserDataSupport.parseBizRef(userData),
                    plain);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签回调报文解析失败", ex);
        }
    }

    private static JsonNode resolveDataNode(JsonNode root) {
        JsonNode msgData = root.get("MsgData");
        if (msgData == null || msgData.isNull()) {
            return root;
        }
        if (msgData.isObject()) {
            return msgData;
        }
        if (msgData.isString() && StringUtils.hasText(msgData.asString())) {
            try {
                return JSON_MAPPER.readTree(msgData.asString());
            } catch (Exception ex) {
                log.warn("电子签回调 MsgData 不是合法 JSON，回退根节点字段");
                return root;
            }
        }
        return root;
    }

    private static String firstText(JsonNode primary, JsonNode fallback, String field) {
        String value = textOrNull(primary, field);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return textOrNull(fallback, field);
    }

    private static String textOrNull(JsonNode root, String field) {
        if (root == null) {
            return null;
        }
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asString();
        return StringUtils.hasText(text) ? text : null;
    }

    /**
     * 是否为子客企业开通/认证类回调。
     *
     * @param msgType 回调消息类型
     * @return 是子客认证回调时为 true
     */
    private static boolean isOrgAuthCallback(String msgType) {
        return "OrgOpenTsignBiz".equalsIgnoreCase(msgType);
    }

    /**
     * 解析子客企业认证回调事件。
     *
     * @param plain 明文 JSON
     * @param base  已解析的流程回调公共字段
     * @return 子客认证事件
     */
    private EssOrgAuthCallbackEvent parseOrgAuthEvent(String plain, EssFlowCallbackEvent base) {
        try {
            JsonNode root = JSON_MAPPER.readTree(plain);
            JsonNode dataNode = resolveDataNode(root);
            String orgOpenId = firstText(dataNode, root, "ProxyOrganizationOpenId");
            String openSuccessRaw = firstText(dataNode, root, "OpenSuccess");
            boolean openSuccess = parseOpenSuccess(openSuccessRaw);
            return new EssOrgAuthCallbackEvent(orgOpenId, openSuccess, plain);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签子客认证回调解析失败", ex);
        }
    }

    private static boolean parseOpenSuccess(String raw) {
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String normalized = raw.trim();
        return "true".equalsIgnoreCase(normalized)
            || "1".equals(normalized)
            || "success".equalsIgnoreCase(normalized);
    }
}
