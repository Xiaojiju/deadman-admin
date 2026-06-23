package com.mtfm.deadman.plugin.pay.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.pay.service.PaymentOrderSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 待支付单主动查单内置 Spring 定时任务。
 * 仅负责触发，具体业务逻辑由 {@link PaymentOrderSyncService} 承担。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay.sync", name = "scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class PaymentOrderSyncScheduler {

    private final PaymentOrderSyncService paymentOrderSyncService;

    /**
     * 按配置 cron 扫描待支付单并主动查单。
     */
    @Scheduled(cron = "${deadman.plugin.pay.sync.cron:0 * * * * ?}")
    public void syncPendingOrders() {
        log.debug("触发内置支付单主动查单任务");
        paymentOrderSyncService.syncPendingOrders();
    }
}
