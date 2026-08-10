package com.mtfm.deadman.plugin.pay.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.pay.service.TransferDispatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 待转明细派发定时任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay.transfer-dispatch",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransferDispatchScheduler {

    private final TransferDispatchService transferDispatchService;

    /**
     * 按 cron 派发待转。
     */
    @Scheduled(cron = "${deadman.plugin.pay.transfer-dispatch.cron:15 * * * * ?}")
    public void dispatch() {
        try {
            int count = transferDispatchService.dispatchPending();
            if (count > 0) {
                log.info("定时派发待转完成，本轮笔数={}", count);
            }
        } catch (RuntimeException ex) {
            log.warn("定时派发待转失败", ex);
        }
    }
}
