package com.mtfm.deadman.plugin.logistics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;

/**
 * 物流 REST API 请求 DTO 聚合。
 */
public final class LogisticsApiRequests {

    private LogisticsApiRequests() {
    }

    /**
     * 物流轨迹订阅请求。
     */
    public record Subscribe(
            @NotBlank String carrierCode,
            @NotBlank String trackingNo,
            String phone,
            String fromAddress,
            String toAddress,
            String callbackUrl,
            String providerId) {

        /**
         * 转为 SPI 订阅上下文。
         *
         * @return 订阅上下文
         */
        public LogisticsSubscribeContext toContext() {
            return new LogisticsSubscribeContext(carrierCode, trackingNo, phone, fromAddress, toAddress, callbackUrl);
        }
    }

    /**
     * 电子面单下单请求。
     */
    public record WaybillOrder(
            @NotBlank String carrierCode,
            String bizOrderId,
            @NotNull @Valid LogisticsContactInfo receiver,
            @NotNull @Valid LogisticsContactInfo sender,
            @NotBlank String cargo,
            double weight,
            int count,
            String remark,
            String payType,
            String expType,
            String tempId,
            String siid,
            boolean needSubscribe,
            String pollCallbackUrl,
            String providerId) {

        /**
         * 转为 SPI 面单上下文。
         *
         * @return 面单上下文
         */
        public LogisticsWaybillOrderContext toContext() {
            return new LogisticsWaybillOrderContext(
                    carrierCode,
                    bizOrderId,
                    receiver,
                    sender,
                    cargo,
                    weight,
                    count <= 0 ? 1 : count,
                    remark,
                    payType,
                    expType,
                    tempId,
                    siid,
                    needSubscribe,
                    pollCallbackUrl);
        }
    }

    /**
     * 电子面单取消请求。
     */
    public record WaybillCancel(
            @NotBlank String trackingNo,
            String carrierCode,
            String taskId,
            String orderId,
            String providerId) {

        /**
         * 转为 SPI 取消上下文。
         *
         * @return 取消上下文
         */
        public LogisticsWaybillCancelContext toContext() {
            return new LogisticsWaybillCancelContext(carrierCode, trackingNo, taskId, orderId);
        }
    }

    /**
     * 商家寄件（官方快递）下单请求。
     */
    public record MerchantShipOrder(
            @NotBlank String carrierCode,
            String bizOrderId,
            @NotNull @Valid LogisticsContactInfo receiver,
            @NotNull @Valid LogisticsContactInfo sender,
            @NotBlank String cargo,
            String weight,
            String remark,
            String serviceType,
            String dayType,
            String pickupStartTime,
            String pickupEndTime,
            String callbackUrl,
            String pollCallbackUrl,
            String payment,
            String providerId) {

        /**
         * 转为 SPI 商家寄件上下文。
         *
         * @return 商家寄件上下文
         */
        public LogisticsMerchantShipOrderContext toContext() {
            return new LogisticsMerchantShipOrderContext(
                    carrierCode,
                    bizOrderId,
                    receiver,
                    sender,
                    cargo,
                    weight,
                    remark,
                    serviceType,
                    dayType,
                    pickupStartTime,
                    pickupEndTime,
                    callbackUrl,
                    pollCallbackUrl,
                    payment);
        }
    }

    /**
     * 商家寄件取消请求。
     */
    public record MerchantShipCancel(
            @NotBlank String orderId, String taskId, String cancelMsg, String providerId) {

        /**
         * 转为 SPI 取消上下文。
         *
         * @return 取消上下文
         */
        public LogisticsMerchantShipCancelContext toContext() {
            return new LogisticsMerchantShipCancelContext(orderId, taskId, cancelMsg);
        }
    }

    /**
     * 商家寄件询价请求。
     */
    public record MerchantShipPrice(
            @NotBlank String carrierCode,
            @NotNull @Valid LogisticsContactInfo sender,
            @NotNull @Valid LogisticsContactInfo receiver,
            String weight,
            String serviceType,
            String providerId) {

        /**
         * 转为 SPI 询价上下文。
         *
         * @return 询价上下文
         */
        public LogisticsMerchantShipPriceContext toContext() {
            return new LogisticsMerchantShipPriceContext(carrierCode, sender, receiver, weight, serviceType);
        }
    }

    /**
     * C 端寄件下单请求。
     */
    public record ConsumerShipOrder(
            @NotBlank String carrierCode,
            @NotNull @Valid LogisticsContactInfo receiver,
            @NotNull @Valid LogisticsContactInfo sender,
            @NotBlank String cargo,
            String weight,
            String remark,
            String dayType,
            String pickupStartTime,
            String pickupEndTime,
            String callbackUrl,
            String payment,
            String expType,
            String providerId) {

        /**
         * 转为 SPI C 端寄件上下文。
         *
         * @return C 端寄件上下文
         */
        public LogisticsConsumerShipOrderContext toContext() {
            return new LogisticsConsumerShipOrderContext(
                    carrierCode,
                    receiver,
                    sender,
                    cargo,
                    weight,
                    remark,
                    dayType,
                    pickupStartTime,
                    pickupEndTime,
                    callbackUrl,
                    payment,
                    expType);
        }
    }

    /**
     * C 端寄件取消请求。
     */
    public record ConsumerShipCancel(
            @NotBlank String orderId, String taskId, String cancelMsg, String providerId) {

        /**
         * 转为 SPI 取消上下文。
         *
         * @return 取消上下文
         */
        public LogisticsConsumerShipCancelContext toContext() {
            return new LogisticsConsumerShipCancelContext(orderId, taskId, cancelMsg);
        }
    }

    /**
     * C 端寄件询价请求。
     */
    public record ConsumerShipPrice(
            @NotBlank String carrierCode,
            @NotNull @Valid LogisticsContactInfo sender,
            @NotNull @Valid LogisticsContactInfo receiver,
            String weight,
            String serviceType,
            String providerId) {

        /**
         * 转为 SPI 询价上下文。
         *
         * @return 询价上下文
         */
        public LogisticsConsumerShipPriceContext toContext() {
            return new LogisticsConsumerShipPriceContext(carrierCode, sender, receiver, weight, serviceType);
        }
    }
}
