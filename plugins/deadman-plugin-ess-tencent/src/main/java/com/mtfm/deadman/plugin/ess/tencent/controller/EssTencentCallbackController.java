package com.mtfm.deadman.plugin.ess.tencent.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.ess.tencent.config.EssTencentPluginProperties;
import com.mtfm.deadman.plugin.ess.tencent.service.EssCallbackDispatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯电子签公开回调入口（验签解密后发布 {@code EssFlowCallbackEvent}，不直接依赖业务）。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.ess-tencent", name = "enabled", havingValue = "true")
public class EssTencentCallbackController {

    private final EssCallbackDispatchService essCallbackDispatchService;
    private final EssTencentPluginProperties properties;

    /**
     * 接收腾讯电子签回调。
     *
     * @param payload          原始回调报文
     * @param contentSignature 请求头 Content-Signature
     * @return 统一成功响应
     */
    @PostMapping("${deadman.plugin.ess-tencent.callback-endpoint:/api/ess/tencent/callback}")
    public Result<Void> callback(
            @RequestBody String payload,
            @RequestHeader(value = "Content-Signature", required = false) String contentSignature) {
        log.debug("收到腾讯电子签回调: endpoint={}", properties.resolveCallbackEndpoint());
        essCallbackDispatchService.dispatch(payload, contentSignature);
        return Result.ok();
    }
}
