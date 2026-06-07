package com.mtfm.deadman.plugin.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.websocket")
public class WebSocketPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** WebSocket 端点路径前缀，实际连接为 {endpointPath}/{channelCode} */
    private String endpointPath = "/ws";

    /** 调度与重试 */
    private Dispatch dispatch = new Dispatch();

    /**
     * 可选：通过 YAML 声明通道（推荐在业务侧用
     * {@link com.mtfm.deadman.plugin.websocket.channel.MessageChannelFactory} 注册
     * Bean）
     */
    private List<Channel> channels = new ArrayList<>();

    @Data
    public static class Dispatch {
        /** 默认最大重试次数 */
        private int maxRetry = 3;
        /** 重试间隔 */
        private Duration retryInterval = Duration.ofSeconds(30);
        /** 定时扫描批次大小 */
        private int batchSize = 50;
        /** 重试扫描 cron */
        private String retryCron = "0/30 * * * * ?";
    }

    @Data
    public static class Channel {
        /** 通道编码 */
        private String code;
        /** 通道说明 */
        private String description;
    }

}
