package com.mtfm.deadman.plugin.pay.wechat.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.service.RefundService;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.provider.WechatEcommerceRefundProvider;
import com.mtfm.deadman.plugin.pay.wechat.util.WechatPayNotifyHttpUtils;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyAck;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信收付通合单退款结果回调 Controller。
 * <p>
 * 本接口响应体遵循微信 APIv3 协议，不使用项目统一 {@code Result} 封装。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-wechat.providers.wechat-ecommerce-combine-jsapi",
        name = "enabled",
        havingValue = "true")
public class WechatEcommerceRefundNotifyController {

    private final RefundService refundService;
    private final WechatPayPluginProperties wechatPayPluginProperties;

    /**
     * 接收微信收付通退款结果回调。
     *
     * @param body    回调请求体原文
     * @param request HTTP 请求
     * @return 微信要求的应答体
     */
    @PostMapping(
            "${deadman.plugin.pay-wechat.providers.wechat-ecommerce-combine-jsapi.refund-notify-endpoint:"
                    + "/client/api/pay/wechat/ecommerce-combine-jsapi/refund/notify}")
    public WechatPayNotifyAck notify(@RequestBody String body, HttpServletRequest request) {
        WechatPayProviderBindingProperties binding =
                wechatPayPluginProperties.providerBinding(WechatPayProviderIds.WECHAT_ECOMMERCE_COMBINE_JSAPI);
        try {
            refundService.handleRefundNotify(
                    WechatEcommerceRefundProvider.PROVIDER_ID,
                    WechatPayNotifyHttpUtils.toNotifyContext(body, request));
            return WechatPayNotifyAck.success();
        } catch (Exception ex) {
            log.warn(
                    "微信收付通退款回调处理失败：provider={}, endpoint={}",
                    WechatEcommerceRefundProvider.PROVIDER_ID,
                    binding.getRefundNotifyEndpoint(),
                    ex);
            return WechatPayNotifyAck.failure();
        }
    }
}
