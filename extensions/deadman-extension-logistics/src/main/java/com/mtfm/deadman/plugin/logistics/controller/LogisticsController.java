package com.mtfm.deadman.plugin.logistics.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.logistics.dto.LogisticsApiRequests;
import com.mtfm.deadman.plugin.logistics.service.LogisticsService;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 物流能力 REST API。
 */
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    /**
     * 实时查询快递轨迹（Redis 短 TTL 缓存）。
     */
    @GetMapping("/tracks")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).TRACK_QUERY)")
    public Result<LogisticsTrackQueryResult> queryTrack(
            @RequestParam("carrierCode") String carrierCode,
            @RequestParam("trackingNo") String trackingNo,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "providerId", required = false) String providerId) {
        return Result.ok(logisticsService.queryTrack(
                new LogisticsTrackQueryContext(carrierCode, trackingNo, phone), providerId));
    }

    /**
     * 智能识别快递公司。
     */
    @GetMapping("/carriers/detect")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).CARRIER_DETECT)")
    public Result<List<LogisticsCarrierDetectResult>> detectCarrier(
            @RequestParam("trackingNo") String trackingNo,
            @RequestParam(value = "providerId", required = false) String providerId) {
        return Result.ok(logisticsService.detectCarrier(trackingNo, providerId));
    }

    /**
     * 订阅快递轨迹推送。
     */
    @PostMapping("/tracks/subscribe")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).TRACK_SUBSCRIBE)")
    public Result<LogisticsSubscribeResult> subscribeTrack(
            @Valid @RequestBody LogisticsApiRequests.Subscribe request) {
        return Result.ok(logisticsService.subscribeTrack(request.toContext(), request.providerId()));
    }

    /**
     * 电子面单下单。
     */
    @PostMapping("/waybills")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).WAYBILL_CREATE)")
    public Result<LogisticsWaybillOrderResult> createWaybill(
            @Valid @RequestBody LogisticsApiRequests.WaybillOrder request) {
        return Result.ok(logisticsService.createWaybill(request.toContext(), request.providerId()));
    }

    /**
     * 取消电子面单。
     */
    @PostMapping("/waybills/cancel")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).WAYBILL_CANCEL)")
    public Result<LogisticsWaybillCancelResult> cancelWaybill(
            @Valid @RequestBody LogisticsApiRequests.WaybillCancel request) {
        return Result.ok(logisticsService.cancelWaybill(request.toContext(), request.providerId()));
    }

    /**
     * 商家寄件下单。
     */
    @PostMapping("/ship/merchant")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).SHIP_MERCHANT_CREATE)")
    public Result<LogisticsMerchantShipOrderResult> createMerchantShipOrder(
            @Valid @RequestBody LogisticsApiRequests.MerchantShipOrder request) {
        return Result.ok(logisticsService.createMerchantShipOrder(request.toContext(), request.providerId()));
    }

    /**
     * 取消商家寄件订单。
     */
    @PostMapping("/ship/merchant/cancel")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).SHIP_MERCHANT_CANCEL)")
    public Result<LogisticsMerchantShipCancelResult> cancelMerchantShipOrder(
            @Valid @RequestBody LogisticsApiRequests.MerchantShipCancel request) {
        return Result.ok(logisticsService.cancelMerchantShipOrder(request.toContext(), request.providerId()));
    }

    /**
     * C 端寄件下单。
     */
    @PostMapping("/ship/consumer")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).SHIP_CONSUMER_CREATE)")
    public Result<LogisticsConsumerShipOrderResult> createConsumerShipOrder(
            @Valid @RequestBody LogisticsApiRequests.ConsumerShipOrder request) {
        return Result.ok(logisticsService.createConsumerShipOrder(request.toContext(), request.providerId()));
    }

    /**
     * 取消 C 端寄件订单。
     */
    @PostMapping("/ship/consumer/cancel")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).SHIP_CONSUMER_CANCEL)")
    public Result<LogisticsConsumerShipCancelResult> cancelConsumerShipOrder(
            @Valid @RequestBody LogisticsApiRequests.ConsumerShipCancel request) {
        return Result.ok(logisticsService.cancelConsumerShipOrder(request.toContext(), request.providerId()));
    }

    /**
     * C 端寄件询价。
     */
    @PostMapping("/ship/consumer/price")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.logistics.permission.LogisticsPermissions).SHIP_CONSUMER_PRICE)")
    public Result<LogisticsConsumerShipPriceResult> queryConsumerShipPrice(
            @Valid @RequestBody LogisticsApiRequests.ConsumerShipPrice request) {
        return Result.ok(logisticsService.queryConsumerShipPrice(request.toContext(), request.providerId()));
    }
}
