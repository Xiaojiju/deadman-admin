package com.mtfm.deadman.plugin.pay.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;

/**
 * 支付插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.pay")
public class PayPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 默认支付 Provider 标识，如 wechat-jsapi */
    private String defaultProvider = "wechat-jsapi";

    /**
     * 测试模式：用于沙箱/联调，可将实际发起支付的金额固定为小额（默认 1 分 = 0.01 元）。
     * <p>
     * 生产环境务必保持 {@code enabled=false}。
     */
    @NestedConfigurationProperty
    private TestMode testMode = new TestMode();

    /** 待支付单主动查单配置 */
    @NestedConfigurationProperty
    private Sync sync = new Sync();

    /** 非终态退款单主动查退款 / 异常退款补偿配置 */
    @NestedConfigurationProperty
    private RefundSync refundSync = new RefundSync();

    /** 异常退款自动处理配置 */
    @NestedConfigurationProperty
    private AbnormalRefund abnormalRefund = new AbnormalRefund();

    /** 商家转账创建限额（总金额/拆单笔数） */
    @NestedConfigurationProperty
    private TransferLimits transferLimits = new TransferLimits();

    /** 待转明细派发调度配置 */
    @NestedConfigurationProperty
    private TransferDispatch transferDispatch = new TransferDispatch();

    /** 非终态转账单主动查单配置 */
    @NestedConfigurationProperty
    private TransferSync transferSync = new TransferSync();

    /**
     * 支付测试模式配置。
     */
    @Data
    public static class TestMode {

        /** 是否启用测试模式（启用后预下单金额将被覆盖） */
        private boolean enabled = false;

        /**
         * 测试模式下固定支付金额（分）。
         * 默认 1 分，即 0.01 元。
         */
        private int fixedAmountCents = 1;
    }

    /**
     * 待支付单主动查单配置。
     */
    @Data
    public static class Sync {

        /** 是否启用内置 Spring 定时查单任务；关闭后可接入 XXL-Job 等外部调度调用 {@code PaymentOrderSyncService} */
        private boolean schedulerEnabled = true;

        /** 内置定时任务 cron 表达式 */
        private String cron = "0 * * * * ?";

        /** 预下单后至少等待多久再查单，避免刚下单即查 */
        private Duration minAge = Duration.ofMinutes(2);

        /** 超过该时间的待支付单不再主动查单 */
        private Duration maxAge = Duration.ofMinutes(30);

        /** 单次扫描最多处理的待支付单数量 */
        private int batchSize = 50;

        /** 是否并行处理扫描到的待支付单 */
        private boolean parallelEnabled = true;

        /**
         * 并行查单使用的线程池 Bean 名称。
         * 配置后优先于插件默认 {@code payOrderSyncExecutor}；未配置且未注入时由插件自建线程池。
         */
        private String executorBeanName;

        /** 默认并行线程数（仅在使用插件自建线程池时生效） */
        private int parallelism = 4;
    }

    /**
     * 非终态退款单主动查退款与异常退款补偿配置。
     */
    @Data
    public static class RefundSync {

        /** 是否启用内置 Spring 定时查退款任务 */
        private boolean schedulerEnabled = true;

        /** 内置定时任务 cron 表达式 */
        private String cron = "30 * * * * ?";

        /** 退款创建后至少等待多久再查 */
        private Duration minAge = Duration.ofMinutes(1);

        /** 超过该时间的非终态退款单不再主动查 */
        private Duration maxAge = Duration.ofHours(24);

        /** 单次扫描最多处理的退款单数量 */
        private int batchSize = 50;

        /** 是否并行处理 */
        private boolean parallelEnabled = true;
    }

    /**
     * 异常退款自动处理配置。
     * <p>
     * 当退款查询/回调结果为 ABNORMAL 时，自动发起渠道异常退款（默认退至商户银行账户）。
     */
    @Data
    public static class AbnormalRefund {

        /**
         * 是否在退款变为 ABNORMAL 时自动发起异常退款。
         * <p>
         * 默认关闭：自动退至商户账户时业务侧不会按「退给买家成功」推进履约，需运营确认后开启或手工处理。
         */
        private boolean autoEnabled = false;

        /**
         * 默认入账方式：MERCHANT_BANK_CARD（商户银行账户）或 USER_BANK_CARD（用户银行卡）。
         * USER_BANK_CARD 需业务侧另行提供银行卡信息并手动调用。
         */
        private String defaultReceiveType = "MERCHANT_BANK_CARD";
    }

    /**
     * 商家转账创建限额。
     */
    @Data
    public static class TransferLimits {

        /**
         * 单次创建最大总金额（分）。
         * 默认 10_000_000 = 10 万元，防止超大拆单压垮库表与调度。
         */
        private long maxAmountCents = 10_000_000L;

        /**
         * 单批次最大拆单笔数。
         * 默认 500。
         */
        private int maxBillsPerBatch = 500;
    }

    /**
     * 待转明细派发调度配置。
     */
    @Data
    public static class TransferDispatch {

        /** 是否启用内置派发定时任务 */
        private boolean schedulerEnabled = true;

        /** 内置定时任务 cron（默认每分钟第 15 秒） */
        private String cron = "15 * * * * ?";

        /** 单次最多派发笔数 */
        private int batchSize = 20;
    }

    /**
     * 非终态转账单主动查单配置。
     */
    @Data
    public static class TransferSync {

        /** 是否启用内置查单定时任务 */
        private boolean schedulerEnabled = true;

        /** 内置定时任务 cron（默认每分钟第 45 秒） */
        private String cron = "45 * * * * ?";

        /** 派发后至少等待多久再查 */
        private Duration minAge = Duration.ofMinutes(1);

        /** 超过该时间的非终态单不再主动查 */
        private Duration maxAge = Duration.ofHours(48);

        /** 单次扫描最多处理数量 */
        private int batchSize = 50;
    }
}
