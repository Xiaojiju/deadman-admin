package com.mtfm.deadman.plugin.pay.wechat.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.service.TransferService;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.provider.WechatJsapiTransferProvider;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHttpUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyAck;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信 JSAPI 商家转账结果回调 Controller。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-jsapi",
        name = "enabled",
        havingValue = "true")
public class WechatJsapiTransferNotifyController {

    private final TransferService transferService;
    private final WechatPayPluginProperties wechatPayPluginProperties;

    /**
     * 接收微信商家转账结果回调。
     *
     * @param body    回调请求体原文
     * @param request HTTP 请求
     * @return 微信要求的应答体
     */
    @PostMapping(
            "${deadman.plugin.pay-wechat.providers.wechat-jsapi.transfer-notify-endpoint:/client/api/pay/wechat/jsapi/transfer/notify}")
    public WechatPayNotifyAck notify(@RequestBody String body, HttpServletRequest request) {
        WechatPayProviderBindingProperties binding =
                wechatPayPluginProperties.providerBinding(WechatPayProviderIds.WECHAT_JSAPI);
        try {
            transferService.handleTransferNotify(
                    WechatJsapiTransferProvider.PROVIDER_ID,
                    WechatPayNotifyHttpUtils.toNotifyContext(body, request));
            return WechatPayNotifyAck.success();
        } catch (Exception ex) {
            log.warn(
                    "微信 JSAPI 商家转账回调处理失败：provider={}, endpoint={}",
                    WechatJsapiTransferProvider.PROVIDER_ID,
                    binding.getTransferNotifyEndpoint(),
                    ex);
            return WechatPayNotifyAck.failure();
        }
    }
}
