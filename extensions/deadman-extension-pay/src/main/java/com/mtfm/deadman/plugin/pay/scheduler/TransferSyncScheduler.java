package com.mtfm.deadman.plugin.pay.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.pay.service.PaymentTransferSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 非终态转账单主动查单定时任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay.transfer-sync",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransferSyncScheduler {

    private final PaymentTransferSyncService paymentTransferSyncService;

    /**
     * 按 cron 查转账。
     */
    @Scheduled(cron = "${deadman.plugin.pay.transfer-sync.cron:45 * * * * ?}")
    public void sync() {
        try {
            paymentTransferSyncService.syncPendingTransfers();
        } catch (RuntimeException ex) {
            log.warn("定时查转账失败", ex);
        }
    }
}
