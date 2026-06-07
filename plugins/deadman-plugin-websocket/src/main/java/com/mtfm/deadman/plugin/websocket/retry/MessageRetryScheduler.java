package com.mtfm.deadman.plugin.websocket.retry;

import com.mtfm.deadman.plugin.websocket.dispatch.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 消息重试定时任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageRetryScheduler {

    private final MessageDispatcher messageDispatcher;

    /**
     * 扫描并重试到期消息。
     */
    @Scheduled(cron = "${deadman.plugin.websocket.dispatch.retry-cron:0/30 * * * * ?}")
    public void retryDueMessages() {
        messageDispatcher.retryDueMessages();
    }
}
