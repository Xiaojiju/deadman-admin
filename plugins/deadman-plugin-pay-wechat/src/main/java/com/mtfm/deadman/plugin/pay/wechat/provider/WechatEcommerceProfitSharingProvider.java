package com.mtfm.deadman.plugin.pay.wechat.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingFinishRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingProvider;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReceiver;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAddProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingFinishCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnResult;

import lombok.RequiredArgsConstructor;

/**
 * 微信平台收付通分账 Provider。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.pay-wechat", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class WechatEcommerceProfitSharingProvider implements ProfitSharingProvider {

    /** Provider 标识 */
    public static final String PROVIDER_ID = "wechat-ecommerce-profitsharing";

    private final WechatPayApiGateway wechatPayApiGateway;

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public ProfitSharingCreateResult create(ProfitSharingCreateRequest request) {
        List<WechatProfitSharingReceiverCommand> receivers = new ArrayList<>();
        for (ProfitSharingReceiver receiver : request.receivers()) {
            receivers.add(new WechatProfitSharingReceiverCommand(
                    receiver.type(), receiver.account(), receiver.amountCents(), receiver.description()));
        }
        WechatProfitSharingCreateResult result =
                wechatPayApiGateway.createEcommerceProfitSharing(new WechatProfitSharingCreateCommand(
                        request.subMchid(),
                        request.transactionId(),
                        request.outOrderNo(),
                        receivers,
                        request.finish()));
        return new ProfitSharingCreateResult(
                result.subMchid(),
                result.transactionId(),
                result.outOrderNo(),
                result.orderId(),
                result.status());
    }

    @Override
    public ProfitSharingQueryResult query(String subMchid, String transactionId, String outOrderNo) {
        WechatProfitSharingQueryResult result =
                wechatPayApiGateway.queryEcommerceProfitSharing(subMchid, transactionId, outOrderNo);
        return new ProfitSharingQueryResult(
                result.subMchid(),
                result.transactionId(),
                result.outOrderNo(),
                result.orderId(),
                result.status(),
                result.receiversRaw());
    }

    @Override
    public ProfitSharingQueryResult finish(ProfitSharingFinishRequest request) {
        WechatProfitSharingCreateResult result =
                wechatPayApiGateway.finishEcommerceProfitSharing(new WechatProfitSharingFinishCommand(
                        request.subMchid(), request.transactionId(), request.outOrderNo(), request.description()));
        return new ProfitSharingQueryResult(
                result.subMchid(),
                result.transactionId(),
                result.outOrderNo(),
                result.orderId(),
                result.status(),
                null);
    }

    @Override
    public ProfitSharingReturnResult returnOrder(ProfitSharingReturnRequest request) {
        WechatProfitSharingReturnResult result =
                wechatPayApiGateway.returnEcommerceProfitSharing(new WechatProfitSharingReturnCommand(
                        request.subMchid(),
                        request.channelOrderId(),
                        request.outOrderNo(),
                        request.outReturnNo(),
                        request.returnMchid(),
                        request.amountCents(),
                        request.description()));
        return new ProfitSharingReturnResult(result.outReturnNo(), result.orderId(), result.result());
    }

    @Override
    public void addReceiver(String appId, String type, String account, String relationType) {
        wechatPayApiGateway.addEcommerceProfitSharingReceiver(
                new WechatAddProfitSharingReceiverCommand(appId, type, account, relationType));
    }
}
