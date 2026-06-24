package com.mtfm.deadman.plugin.logistics.kuaidi100.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.kuaidi100.sdk.api.AutoNum;
import com.kuaidi100.sdk.api.BOrderOfficial;
import com.kuaidi100.sdk.api.COrder;
import com.kuaidi100.sdk.api.LabelV2;
import com.kuaidi100.sdk.api.QueryTrack;
import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.AutoNumReq;
import com.kuaidi100.sdk.request.BOrderCancelReq;
import com.kuaidi100.sdk.request.BOrderOfficialQueryPriceReq;
import com.kuaidi100.sdk.request.BOrderReq;
import com.kuaidi100.sdk.request.ManInfo;
import com.kuaidi100.sdk.request.PrintReq;
import com.kuaidi100.sdk.request.QueryTrackParam;
import com.kuaidi100.sdk.request.QueryTrackReq;
import com.kuaidi100.sdk.request.SubscribeParam;
import com.kuaidi100.sdk.request.SubscribeParameters;
import com.kuaidi100.sdk.request.SubscribeReq;
import com.kuaidi100.sdk.request.corder.COrderQueryPriceReq;
import com.kuaidi100.sdk.request.corder.COrderReq;
import com.kuaidi100.sdk.request.labelV2.BackOrderReq;
import com.kuaidi100.sdk.request.labelV2.OrderReq;
import com.kuaidi100.sdk.response.AutoNumResp;
import com.kuaidi100.sdk.response.PrintBaseResp;
import com.kuaidi100.sdk.response.QueryTrackResp;
import com.kuaidi100.sdk.response.SubscribeResp;
import com.kuaidi100.sdk.response.labelV2.OrderResult;
import com.kuaidi100.sdk.response.labelV2.Result;
import com.kuaidi100.sdk.utils.SignUtils;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.logistics.kuaidi100.config.Kuaidi100LogisticsPluginProperties;
import com.mtfm.deadman.plugin.logistics.kuaidi100.constant.Kuaidi100ProviderIds;
import com.mtfm.deadman.plugin.logistics.kuaidi100.util.Kuaidi100PrintRequestBuilder;
import com.mtfm.deadman.plugin.logistics.kuaidi100.util.Kuaidi100TrackMapper;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.common.LogisticsContactInfo;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsConsumerShipPriceResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipOrderResult;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipPriceContext;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsMerchantShipPriceResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribeResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillCancelResult;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderContext;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillOrderResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 快递100 API 真实网关实现，基于官方 SDK 调用查单、订阅、面单与寄件接口。
 */
@Slf4j
@RequiredArgsConstructor
public class Kuaidi100LogisticsApiGatewayImpl implements Kuaidi100LogisticsApiGateway {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final Kuaidi100LogisticsPluginProperties properties;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsTrackQueryResult queryTrack(LogisticsTrackQueryContext context) {
        validateQueryCredentials();
        try {
            QueryTrackParam trackParam = buildTrackParam(context);
            String paramJson = JSON_MAPPER.writeValueAsString(trackParam);

            QueryTrackReq request = new QueryTrackReq();
            request.setParam(paramJson);
            request.setCustomer(properties.getCustomer());
            request.setSign(SignUtils.querySign(paramJson, properties.getKey(), properties.getCustomer()));

            QueryTrackResp response = new QueryTrack().queryTrack(request);
            return mapTrackResponse(context, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "快递100查单失败：carrier={}, trackingNo={}",
                    context.carrierCode(),
                    context.trackingNo(),
                    ex);
            throw new BusinessException(ResultCode.LOGISTICS_TRACK_QUERY_FAILED, "快递100查单失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogisticsCarrierDetectResult> detectCarrier(String trackingNo) {
        validateKey();
        try {
            AutoNumReq request = new AutoNumReq();
            request.setKey(properties.getKey());
            request.setNum(trackingNo);

            List<AutoNumResp> responses = new AutoNum().getComByNumList(request);
            if (responses == null || responses.isEmpty()) {
                return List.of();
            }
            List<LogisticsCarrierDetectResult> results = new ArrayList<>(responses.size());
            for (AutoNumResp item : responses) {
                results.add(new LogisticsCarrierDetectResult(item.getComCode(), item.getName(), item.getLengthPre()));
            }
            return Collections.unmodifiableList(results);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100快递公司识别失败：trackingNo={}", trackingNo, ex);
            throw new BusinessException(ResultCode.LOGISTICS_CARRIER_DETECT_FAILED, "快递100识别失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribeResult subscribeTrack(LogisticsSubscribeContext context) {
        validateKey();
        try {
            SubscribeParam subscribeParam = buildSubscribeParam(context);
            String paramJson = JSON_MAPPER.writeValueAsString(subscribeParam);

            SubscribeReq request = new SubscribeReq();
            request.setSchema(ApiInfoConstant.SUBSCRIBE_SCHEMA);
            request.setParam(paramJson);

            SubscribeResp response = new Subscribe().subscribe(request);
            if (response == null) {
                throw new BusinessException(ResultCode.LOGISTICS_SUBSCRIBE_FAILED, "快递100订阅返回为空");
            }
            return new LogisticsSubscribeResult(
                    response.isResult(), response.getReturnCode(), response.getMessage());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "快递100轨迹订阅失败：carrier={}, trackingNo={}",
                    context.carrierCode(),
                    context.trackingNo(),
                    ex);
            throw new BusinessException(ResultCode.LOGISTICS_SUBSCRIBE_FAILED, "快递100订阅失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsSubscribePushPayload parseSubscribePush(String rawParam, String sign) {
        if (!StringUtils.hasText(rawParam) || !StringUtils.hasText(sign)) {
            return null;
        }
        if (!StringUtils.hasText(properties.getSubscribeSalt())) {
            log.warn("快递100订阅 salt 未配置，无法验签");
            return null;
        }
        String expectedSign = SignUtils.sign(rawParam + properties.getSubscribeSalt());
        if (!expectedSign.equalsIgnoreCase(sign)) {
            log.warn("快递100订阅推送验签失败");
            return null;
        }
        return Kuaidi100TrackMapper.parseSubscribePushPayload(Kuaidi100ProviderIds.KUAIDI100, rawParam);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsWaybillOrderResult createWaybill(LogisticsWaybillOrderContext context) {
        try {
            OrderReq orderReq = buildWaybillOrderReq(context);
            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.ORDER, orderReq);
            Result<OrderResult> response = new LabelV2().order(printReq);
            if (response == null || !response.isSuccess()) {
                String message = response == null ? "面单下单返回为空" : response.getMessage();
                throw new BusinessException(ResultCode.LOGISTICS_WAYBILL_FAILED, message);
            }
            OrderResult data = response.getData();
            return new LogisticsWaybillOrderResult(
                    true,
                    response.getMessage(),
                    data == null ? null : data.getKuaidinum(),
                    data == null ? null : data.getTaskId(),
                    data == null ? null : data.getLabel(),
                    data == null ? null : data.getPrintData(),
                    data == null ? null : data.getKdComOrderNum());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100电子面单下单失败：carrier={}, bizOrderId={}", context.carrierCode(), context.bizOrderId(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_WAYBILL_FAILED, "快递100面单下单失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsWaybillCancelResult cancelWaybill(LogisticsWaybillCancelContext context) {
        try {
            BackOrderReq backOrderReq = new BackOrderReq();
            backOrderReq.setKuaidicom(context.carrierCode());
            backOrderReq.setKuaidinum(context.trackingNo());
            backOrderReq.setOrderId(StringUtils.hasText(context.orderId()) ? context.orderId() : context.taskId());

            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.BACKORDER, backOrderReq);
            Result<?> response = new LabelV2().backOrder(printReq);
            if (response == null || !response.isSuccess()) {
                String message = response == null ? "面单取消返回为空" : response.getMessage();
                return new LogisticsWaybillCancelResult(false, message);
            }
            return new LogisticsWaybillCancelResult(true, response.getMessage());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100电子面单取消失败：trackingNo={}", context.trackingNo(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_WAYBILL_FAILED, "快递100面单取消失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipOrderResult createMerchantShipOrder(LogisticsMerchantShipOrderContext context) {
        try {
            BOrderReq orderReq = buildOfficialMerchantShipOrderReq(context);
            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.B_ORDER_OFFICIAL_ORDER_METHOD, orderReq);
            HttpResult httpResult = new BOrderOfficial().execute(printReq);
            PrintBaseResp<?> response = parsePrintBaseResp(httpResult);
            return mapMerchantShipOrderResult(response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100商家官方寄件下单失败：carrier={}, bizOrderId={}", context.carrierCode(), context.bizOrderId(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "快递100商家官方寄件下单失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipCancelResult cancelMerchantShipOrder(LogisticsMerchantShipCancelContext context) {
        try {
            BOrderCancelReq cancelReq = new BOrderCancelReq();
            cancelReq.setOrderId(context.orderId());
            cancelReq.setTaskId(context.taskId());
            cancelReq.setCancelMsg(context.cancelMsg());

            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.B_ORDER_OFFICIAL_CANCEL_METHOD, cancelReq);
            HttpResult httpResult = new BOrderOfficial().execute(printReq);
            PrintBaseResp<?> response = parsePrintBaseResp(httpResult);
            return mapMerchantShipCancelResult(response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100商家官方寄件取消失败：orderId={}", context.orderId(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_CANCEL_FAILED, "快递100商家官方寄件取消失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsMerchantShipPriceResult queryMerchantShipPrice(LogisticsMerchantShipPriceContext context) {
        try {
            BOrderOfficialQueryPriceReq priceReq = new BOrderOfficialQueryPriceReq();
            priceReq.setKuaidiCom(context.carrierCode());
            priceReq.setSendManPrintAddr(context.sender().printAddress());
            priceReq.setRecManPrintAddr(context.receiver().printAddress());
            priceReq.setWeight(context.weight());
            priceReq.setServiceType(context.serviceType());

            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.B_ORDER_OFFICIAL_PRICE_METHOD, priceReq);
            HttpResult httpResult = new BOrderOfficial().execute(printReq);
            return mapMerchantShipPriceResult(httpResult);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100商家官方寄件询价失败：carrier={}", context.carrierCode(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "快递100商家官方寄件询价失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipOrderResult createConsumerShipOrder(LogisticsConsumerShipOrderContext context) {
        try {
            COrderReq orderReq = buildConsumerShipOrderReq(context);
            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.C_ORDER_METHOD, orderReq);
            HttpResult httpResult = new COrder().execute(printReq);
            PrintBaseResp<?> response = parsePrintBaseResp(httpResult);
            return mapConsumerShipOrderResult(response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100 C 端寄件下单失败：carrier={}", context.carrierCode(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "快递100 C 端寄件下单失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipCancelResult cancelConsumerShipOrder(LogisticsConsumerShipCancelContext context) {
        try {
            BOrderCancelReq cancelReq = new BOrderCancelReq();
            cancelReq.setOrderId(context.orderId());
            cancelReq.setTaskId(context.taskId());
            cancelReq.setCancelMsg(context.cancelMsg());

            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.CANCEL_METHOD, cancelReq);
            HttpResult httpResult = new COrder().execute(printReq);
            PrintBaseResp<?> response = parsePrintBaseResp(httpResult);
            return mapConsumerShipCancelResult(response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100 C 端寄件取消失败：orderId={}", context.orderId(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_CANCEL_FAILED, "快递100 C 端寄件取消失败：" + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogisticsConsumerShipPriceResult queryConsumerShipPrice(LogisticsConsumerShipPriceContext context) {
        try {
            COrderQueryPriceReq priceReq = new COrderQueryPriceReq();
            priceReq.setKuaidicom(context.carrierCode());
            priceReq.setSendManPrintAddr(context.sender().printAddress());
            priceReq.setRecManPrintAddr(context.receiver().printAddress());
            priceReq.setWeight(context.weight());
            priceReq.setServiceType(context.serviceType());

            PrintReq printReq = printRequestBuilder().build(ApiInfoConstant.C_ORDER_PRICE_METHOD, priceReq);
            HttpResult httpResult = new COrder().execute(printReq);
            return mapConsumerShipPriceResult(httpResult);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("快递100 C 端寄件询价失败：carrier={}", context.carrierCode(), ex);
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "快递100 C 端寄件询价失败：" + ex.getMessage());
        }
    }

    private Kuaidi100PrintRequestBuilder printRequestBuilder() {
        return new Kuaidi100PrintRequestBuilder(properties);
    }

    private void validateQueryCredentials() {
        if (!StringUtils.hasText(properties.getKey()) || !StringUtils.hasText(properties.getCustomer())) {
            throw new BusinessException(ResultCode.LOGISTICS_CONFIG_INVALID, "快递100 key 或 customer 未配置");
        }
    }

    private void validateKey() {
        if (!StringUtils.hasText(properties.getKey())) {
            throw new BusinessException(ResultCode.LOGISTICS_CONFIG_INVALID, "快递100 key 未配置");
        }
    }

    private QueryTrackParam buildTrackParam(LogisticsTrackQueryContext context) {
        QueryTrackParam trackParam = new QueryTrackParam();
        trackParam.setCom(context.carrierCode());
        trackParam.setNum(context.trackingNo());
        if (StringUtils.hasText(context.phone())) {
            trackParam.setPhone(context.phone());
        }
        trackParam.setResultv2("4");
        return trackParam;
    }

    private LogisticsTrackQueryResult mapTrackResponse(LogisticsTrackQueryContext context, QueryTrackResp response) {
        if (response == null) {
            throw new BusinessException(ResultCode.LOGISTICS_TRACK_QUERY_FAILED, "快递100返回为空");
        }
        if (!response.isResult()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : "查单失败";
            throw new BusinessException(ResultCode.LOGISTICS_TRACK_QUERY_FAILED, message);
        }
        return Kuaidi100TrackMapper.toQueryResult(Kuaidi100ProviderIds.KUAIDI100, context, response);
    }

    private SubscribeParam buildSubscribeParam(LogisticsSubscribeContext context) {
        SubscribeParameters parameters = new SubscribeParameters();
        parameters.setCallbackurl(properties.resolveSubscribeCallbackUrl(context.callbackUrl()));
        parameters.setSalt(properties.getSubscribeSalt());
        parameters.setResultv2("4");
        if (StringUtils.hasText(context.phone())) {
            parameters.setPhone(context.phone());
        }

        SubscribeParam subscribeParam = new SubscribeParam();
        subscribeParam.setCompany(context.carrierCode());
        subscribeParam.setNumber(context.trackingNo());
        subscribeParam.setFrom(context.fromAddress());
        subscribeParam.setTo(context.toAddress());
        subscribeParam.setKey(properties.getKey());
        subscribeParam.setParameters(parameters);
        return subscribeParam;
    }

    private OrderReq buildWaybillOrderReq(LogisticsWaybillOrderContext context) {
        OrderReq orderReq = new OrderReq();
        orderReq.setKuaidicom(context.carrierCode());
        orderReq.setOrderId(context.bizOrderId());
        orderReq.setRecMan(toManInfo(context.receiver()));
        orderReq.setSendMan(toManInfo(context.sender()));
        orderReq.setCargo(context.cargo());
        orderReq.setWeight(context.weight());
        orderReq.setCount(context.count());
        orderReq.setRemark(context.remark());
        if (StringUtils.hasText(context.payType())) {
            orderReq.setPayType(context.payType());
        }
        if (StringUtils.hasText(context.expType())) {
            orderReq.setExpType(context.expType());
        }
        if (StringUtils.hasText(context.tempId())) {
            orderReq.setTempId(context.tempId());
        }
        if (StringUtils.hasText(context.siid())) {
            orderReq.setSiid(context.siid());
        }
        orderReq.setNeedSubscribe(context.needSubscribe());
        if (context.needSubscribe()) {
            orderReq.setPollCallBackUrl(
                    properties.resolveSubscribeCallbackUrl(context.pollCallbackUrl()));
            orderReq.setSalt(properties.getSubscribeSalt());
        }
        orderReq.setResultv2("4");
        return orderReq;
    }

    private BOrderReq buildOfficialMerchantShipOrderReq(LogisticsMerchantShipOrderContext context) {
        BOrderReq orderReq = new BOrderReq();
        orderReq.setKuaidicom(context.carrierCode());
        orderReq.setThirdOrderId(context.bizOrderId());
        fillOfficialContact(orderReq, context.sender(), context.receiver());
        orderReq.setCargo(context.cargo());
        orderReq.setWeight(context.weight());
        orderReq.setRemark(context.remark());
        orderReq.setServiceType(context.serviceType());
        orderReq.setDayType(context.dayType());
        orderReq.setPickupStartTime(context.pickupStartTime());
        orderReq.setPickupEndTime(context.pickupEndTime());
        orderReq.setCallBackUrl(properties.resolveMerchantShipCallbackUrl(context.callbackUrl()));
        orderReq.setPollCallBackUrl(properties.resolveSubscribeCallbackUrl(context.pollCallbackUrl()));
        orderReq.setSalt(properties.getSubscribeSalt());
        if (StringUtils.hasText(context.payment())) {
            orderReq.setPayment(context.payment());
        }
        orderReq.setResultv2("4");
        return orderReq;
    }

    private COrderReq buildConsumerShipOrderReq(LogisticsConsumerShipOrderContext context) {
        COrderReq orderReq = new COrderReq();
        orderReq.setKuaidicom(context.carrierCode());
        fillContact(orderReq, context.sender(), context.receiver());
        orderReq.setCargo(context.cargo());
        orderReq.setWeight(context.weight());
        orderReq.setRemark(context.remark());
        orderReq.setDayType(context.dayType());
        orderReq.setPickupStartTime(context.pickupStartTime());
        orderReq.setPickupEndTime(context.pickupEndTime());
        orderReq.setCallBackUrl(properties.resolveConsumerShipCallbackUrl(context.callbackUrl()));
        orderReq.setPayment(context.payment());
        orderReq.setExpType(context.expType());
        orderReq.setSalt(properties.getSubscribeSalt());
        orderReq.setResultv2("4");
        return orderReq;
    }

    private void fillOfficialContact(BOrderReq orderReq, LogisticsContactInfo sender, LogisticsContactInfo receiver) {
        orderReq.setSendManName(sender.name());
        orderReq.setSendManMobile(sender.mobile());
        orderReq.setSendManPrintAddr(sender.printAddress());
        orderReq.setRecManName(receiver.name());
        orderReq.setRecManMobile(receiver.mobile());
        orderReq.setRecManPrintAddr(receiver.printAddress());
    }

    private void fillContact(COrderReq orderReq, LogisticsContactInfo sender, LogisticsContactInfo receiver) {
        orderReq.setSendManName(sender.name());
        orderReq.setSendManMobile(sender.mobile());
        orderReq.setSendManPrintAddr(sender.printAddress());
        orderReq.setRecManName(receiver.name());
        orderReq.setRecManMobile(receiver.mobile());
        orderReq.setRecManPrintAddr(receiver.printAddress());
    }

    private ManInfo toManInfo(LogisticsContactInfo contact) {
        ManInfo manInfo = new ManInfo();
        manInfo.setName(contact.name());
        manInfo.setMobile(contact.mobile());
        manInfo.setPrintAddr(contact.printAddress());
        return manInfo;
    }

    @SuppressWarnings("unchecked")
    private PrintBaseResp<Map<String, Object>> parsePrintBaseResp(HttpResult httpResult) {
        if (httpResult == null || !StringUtils.hasText(httpResult.getBody())) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "快递100返回为空");
        }
        try {
            JsonNode root = JSON_MAPPER.readTree(httpResult.getBody());
            PrintBaseResp<Map<String, Object>> response = new PrintBaseResp<>();
            response.setReturnCode(textValue(root, "returnCode"));
            response.setMessage(textValue(root, "message"));
            response.setResult(root.path("result").asBoolean(false));
            JsonNode dataNode = root.get("data");
            if (dataNode != null && !dataNode.isNull()) {
                response.setData(JSON_MAPPER.convertValue(dataNode, Map.class));
            }
            return response;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "解析快递100响应失败：" + ex.getMessage());
        }
    }

    private LogisticsMerchantShipOrderResult mapMerchantShipOrderResult(PrintBaseResp<?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "商家官方寄件返回为空");
        }
        if (!response.isResult()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : "商家官方寄件下单失败";
            return new LogisticsMerchantShipOrderResult(false, message, null, null, null);
        }
        Map<String, Object> data = asDataMap(response.getData());
        return new LogisticsMerchantShipOrderResult(
                true,
                response.getMessage(),
                extractString(data, "orderId"),
                extractString(data, "kuaidinum"),
                extractString(data, "taskId"));
    }

    private LogisticsMerchantShipCancelResult mapMerchantShipCancelResult(PrintBaseResp<?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_CANCEL_FAILED, "商家官方寄件取消返回为空");
        }
        if (!response.isResult()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : "商家官方寄件取消失败";
            return new LogisticsMerchantShipCancelResult(false, message);
        }
        return new LogisticsMerchantShipCancelResult(true, response.getMessage());
    }

    private LogisticsMerchantShipPriceResult mapMerchantShipPriceResult(HttpResult httpResult) {
        if (httpResult == null || !StringUtils.hasText(httpResult.getBody())) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "商家官方寄件询价返回为空");
        }
        String rawData = httpResult.getBody();
        try {
            JsonNode root = JSON_MAPPER.readTree(rawData);
            boolean success = root.path("result").asBoolean(false);
            String message = textValue(root, "message");
            if (!success) {
                return new LogisticsMerchantShipPriceResult(false, message, null, rawData);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = root.has("data") && !root.get("data").isNull()
                    ? JSON_MAPPER.convertValue(root.get("data"), Map.class)
                    : Map.of();
            String price = firstNonBlank(
                    extractString(data, "price"),
                    extractString(data, "defPrice"),
                    extractString(data, "totalPrice"));
            return new LogisticsMerchantShipPriceResult(true, message, price, rawData);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "解析商家官方寄件询价响应失败：" + ex.getMessage());
        }
    }

    private LogisticsConsumerShipOrderResult mapConsumerShipOrderResult(PrintBaseResp<?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "C 端寄件返回为空");
        }
        if (!response.isResult()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : "C 端寄件下单失败";
            return new LogisticsConsumerShipOrderResult(false, message, null, null);
        }
        Map<String, Object> data = asDataMap(response.getData());
        return new LogisticsConsumerShipOrderResult(
                true,
                response.getMessage(),
                extractString(data, "orderId"),
                extractString(data, "taskId"));
    }

    private LogisticsConsumerShipCancelResult mapConsumerShipCancelResult(PrintBaseResp<?> response) {
        if (response == null) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_CANCEL_FAILED, "C 端寄件取消返回为空");
        }
        if (!response.isResult()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : "C 端寄件取消失败";
            return new LogisticsConsumerShipCancelResult(false, message);
        }
        return new LogisticsConsumerShipCancelResult(true, response.getMessage());
    }

    private LogisticsConsumerShipPriceResult mapConsumerShipPriceResult(HttpResult httpResult) {
        if (httpResult == null || !StringUtils.hasText(httpResult.getBody())) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "C 端寄件询价返回为空");
        }
        String rawData = httpResult.getBody();
        try {
            JsonNode root = JSON_MAPPER.readTree(rawData);
            boolean success = root.path("result").asBoolean(false);
            String message = textValue(root, "message");
            if (!success) {
                return new LogisticsConsumerShipPriceResult(false, message, null, rawData);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = root.has("data") && !root.get("data").isNull()
                    ? JSON_MAPPER.convertValue(root.get("data"), Map.class)
                    : Map.of();
            String price = firstNonBlank(
                    extractString(data, "price"),
                    extractString(data, "defPrice"),
                    extractString(data, "totalPrice"));
            return new LogisticsConsumerShipPriceResult(true, message, price, rawData);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.LOGISTICS_SHIP_ORDER_FAILED, "解析 C 端寄件询价响应失败：" + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asDataMap(Object data) {
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String extractString(Map<String, Object> data, String key) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
