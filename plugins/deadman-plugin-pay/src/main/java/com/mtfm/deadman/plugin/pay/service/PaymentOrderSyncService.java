package com.mtfm.deadman.plugin.pay.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderSnapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 待支付单主动查单业务服务。
 * 与定时触发解耦，宿主可关闭内置调度后自行调用本服务接入外部任务框架。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderSyncService {

    private final PayService payService;
    private final PaymentOrderService paymentOrderService;
    private final PayPluginProperties payPluginProperties;

    /**
     * 扫描并同步符合条件的待支付单。
     * 供内置定时任务或外部调度框架调用。
     */
    public void syncPendingOrders() {
        PayPluginProperties.Sync sync = payPluginProperties.getSync();
        List<PaymentOrder> pendingOrders = paymentOrderService.listPendingForSync(
                sync.getMinAge(), sync.getMaxAge(), sync.getBatchSize());
        if (pendingOrders.isEmpty()) {
            log.debug("无待主动查单的支付单");
            return;
        }
        log.info("开始主动查单，待处理数量={}", pendingOrders.size());
        for (PaymentOrder order : pendingOrders) {
            syncOrderSafely(order.getOutTradeNo());
        }
    }

    /**
     * 主动查单并同步单笔支付单状态。
     *
     * @param outTradeNo 平台支付单号
     * @return 同步后的支付单快照
     */
    public PaymentOrderSnapshot syncOrder(String outTradeNo) {
        return payService.syncOrderFromChannel(outTradeNo);
    }

    private void syncOrderSafely(String outTradeNo) {
        try {
            PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel(outTradeNo);
            log.debug("主动查单完成：outTradeNo={}, status={}", outTradeNo, snapshot.status());
        } catch (RuntimeException ex) {
            log.warn("主动查单失败：outTradeNo={}", outTradeNo, ex);
        }
    }
}
