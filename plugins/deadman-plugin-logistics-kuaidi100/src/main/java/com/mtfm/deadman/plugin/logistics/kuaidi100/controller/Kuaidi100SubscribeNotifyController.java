package com.mtfm.deadman.plugin.logistics.kuaidi100.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.kuaidi100.vo.Kuaidi100SubscribeNotifyAck;
import com.mtfm.deadman.plugin.logistics.service.LogisticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 快递100 轨迹订阅推送回调 Controller，接收渠道通知后委托 LogisticsService 统一处理。
 * <p>
 * 本接口响应体遵循快递100 回调协议，不使用项目统一 {@code Result} 封装。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.logistics-kuaidi100", name = "enabled", havingValue = "true")
public class Kuaidi100SubscribeNotifyController {

    private final LogisticsService logisticsService;

    /**
     * 接收快递100 轨迹订阅推送。
     *
     * @param param 推送 param JSON
     * @param sign  推送签名
     * @return 快递100 要求的应答体
     */
    @PostMapping("${deadman.plugin.logistics-kuaidi100.subscribe-notify-endpoint:/client/api/logistics/kuaidi100/subscribe/notify}")
    public Kuaidi100SubscribeNotifyAck notify(@RequestParam("param") String param, @RequestParam("sign") String sign) {
        try {
            logisticsService.handleSubscribePush(Kuaidi100ProviderIds.KUAIDI100, param, sign);
            return Kuaidi100SubscribeNotifyAck.success();
        } catch (Exception ex) {
            log.warn("快递100订阅推送处理失败：provider={}", Kuaidi100ProviderIds.KUAIDI100, ex);
            return Kuaidi100SubscribeNotifyAck.failure();
        }
    }
}
