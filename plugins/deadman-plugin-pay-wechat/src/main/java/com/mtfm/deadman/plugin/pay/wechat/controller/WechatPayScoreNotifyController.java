package com.mtfm.deadman.plugin.pay.wechat.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.facade.PayScoreFacade;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreNotifyResult;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.provider.WechatPayScoreProvider;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHttpUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyAck;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付分结果回调 Controller（仅验签解析并打日志，不落库、不调业务）。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-payscore",
        name = "enabled",
        havingValue = "true")
public class WechatPayScoreNotifyController {

    private final PayScoreFacade payScoreFacade;
    private final WechatPayPluginProperties wechatPayPluginProperties;

    /**
     * 接收微信支付分结果回调。
     *
     * @param body    回调请求体原文
     * @param request HTTP 请求
     * @return 微信要求的应答体
     */
    @PostMapping(
            "${deadman.plugin.pay-wechat.providers.wechat-payscore.pay-score-notify-endpoint:/client/api/pay/wechat/payscore/notify}")
    public WechatPayNotifyAck notify(@RequestBody String body, HttpServletRequest request) {
        WechatPayProviderBindingProperties binding =
                wechatPayPluginProperties.providerBinding(WechatPayProviderIds.WECHAT_PAY_SCORE);
        try {
            PayScoreNotifyResult result = payScoreFacade.parseNotify(
                    WechatPayScoreProvider.PROVIDER_ID,
                    WechatPayNotifyHttpUtils.toNotifyContext(body, request));
            log.info(
                    "微信支付分回调已解析：provider={}, outOrderNo={}, channelOrderId={}, state={}, eventType={}",
                    WechatPayScoreProvider.PROVIDER_ID,
                    result.getOutOrderNo(),
                    result.getChannelOrderId(),
                    result.getState(),
                    result.getEventType());
            return WechatPayNotifyAck.success();
        } catch (Exception ex) {
            log.warn(
                    "微信支付分回调处理失败：provider={}, endpoint={}",
                    WechatPayScoreProvider.PROVIDER_ID,
                    binding.getPayScoreNotifyEndpoint(),
                    ex);
            return WechatPayNotifyAck.failure();
        }
    }
}
