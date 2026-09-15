package com.mtfm.deadman.plugin.logistics.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.logistics.service.LogisticsService;
import com.mtfm.deadman.plugin.logistics.vo.LogisticsCarrierOptionVO;

import lombok.RequiredArgsConstructor;

/**
 * 用户端物流能力 API（小程序选择快递公司等）。
 */
@RestController
@RequestMapping("/client/api/logistics")
@RequiredArgsConstructor
public class LogisticsClientController {

    private final LogisticsService logisticsService;

    /**
     * 列出当前渠道已注册、可供选择的快递公司。
     *
     * @param providerId Provider 标识，为空时使用默认渠道
     * @return 快递公司列表（统一编码 + 名称）
     */
    @GetMapping("/carriers")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<List<LogisticsCarrierOptionVO>> listCarriers(
            @RequestParam(value = "providerId", required = false) String providerId) {
        return Result.ok(logisticsService.listCarriers(providerId));
    }
}
