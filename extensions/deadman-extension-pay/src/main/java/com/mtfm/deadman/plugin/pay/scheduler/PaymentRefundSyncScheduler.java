package com.mtfm.deadman.plugin.pay.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.pay.service.PaymentRefundSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 非终态退款单主动查退款内置 Spring 定时任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay.refund-sync",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PaymentRefundSyncScheduler {

    private final PaymentRefundSyncService paymentRefundSyncService;

    /**
     * 按配置 cron 扫描非终态退款单并主动查退款/补偿异常退款。
     */
    @Scheduled(cron = "${deadman.plugin.pay.refund-sync.cron:30 * * * * ?}")
    public void syncPendingRefunds() {
        log.debug("触发内置退款单主动查退款任务");
        paymentRefundSyncService.syncPendingRefunds();
    }
}
