package com.mtfm.deadman.plugin.pay.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.dto.transfer.CreateTransferRequest;
import com.mtfm.deadman.plugin.pay.dto.transfer.TransferBatchPageQuery;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBatch;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBill;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferQuota;
import com.mtfm.deadman.plugin.pay.manager.TransferProviderManager;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferProvider;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferQueryResult;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBatchVO;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBillVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 商家转账门面：拆单落库、回调/查单回写、批次查询。
 * <p>
 * 创建仅落 PENDING；实际渠道发起由 {@link TransferDispatchService} 定时/手动派发。
 */
@Slf4j
@Service
public class TransferService {

    private final TransferProviderManager transferProviderManager;
    private final TransferQuotaService transferQuotaService;
    private final PaymentTransferOrderService paymentTransferOrderService;
    private final TransferDispatchService transferDispatchService;
    private final PayPluginProperties payPluginProperties;
    private final PaySensitiveCryptoHelper paySensitiveCryptoHelper;

    /**
     * @param transferProviderManager     转账 Provider 管理器
     * @param transferQuotaService        额度服务
     * @param paymentTransferOrderService 批次/明细服务
     * @param transferDispatchService     派发服务（Lazy 打断循环依赖）
     * @param payPluginProperties         插件配置
     * @param paySensitiveCryptoHelper    敏感字段加解密
     */
    public TransferService(
            TransferProviderManager transferProviderManager,
            TransferQuotaService transferQuotaService,
            PaymentTransferOrderService paymentTransferOrderService,
            @Lazy TransferDispatchService transferDispatchService,
            PayPluginProperties payPluginProperties,
            PaySensitiveCryptoHelper paySensitiveCryptoHelper) {
        this.transferProviderManager = transferProviderManager;
        this.transferQuotaService = transferQuotaService;
        this.paymentTransferOrderService = paymentTransferOrderService;
        this.transferDispatchService = transferDispatchService;
        this.payPluginProperties = payPluginProperties;
        this.paySensitiveCryptoHelper = paySensitiveCryptoHelper;
    }

    /**
     * 发起商家转账：按单笔限额拆成多笔待转；派发交调度，本接口不外呼渠道。
     * <p>
     * 同一 {@code bizOrderNo} 幂等返回原批次。
     *
     * @param request 转账请求
     * @return 批次视图
     */
    public TransferBatchVO createTransfer(CreateTransferRequest request) {
        if (request.amountCents() == null || request.amountCents() <= 0) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_AMOUNT_INVALID, "转账金额必须大于 0");
        }
        String bizOrderNo = request.bizOrderNo().trim();
        Optional<PaymentTransferBatch> existing = paymentTransferOrderService.findByBizOrderNo(bizOrderNo);
        if (existing.isPresent()) {
            assertIdempotentMatch(existing.get(), request);
            log.info(
                    "转账业务单号已存在，幂等返回：bizOrderNo={}, batchNo={}",
                    bizOrderNo,
                    existing.get().getBatchNo());
            return getBatch(existing.get().getBatchNo());
        }
        PayPluginProperties.TransferLimits limits = payPluginProperties.getTransferLimits();
        if (request.amountCents() > limits.getMaxAmountCents()) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_LIMIT_EXCEEDED,
                    "转账总金额超过平台上限：" + limits.getMaxAmountCents() + " 分");
        }
        TransferProvider provider = transferProviderManager.require(request.providerId());
        PaymentTransferQuota quota = transferQuotaService.requireCurrent();
        long singleLimit = quota.getSingleLimitCents();
        if (singleLimit < 1) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_QUOTA_INVALID, "单笔限额配置无效");
        }
        List<Long> splits = splitAmount(request.amountCents(), singleLimit);
        if (splits.size() > limits.getMaxBillsPerBatch()) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_LIMIT_EXCEEDED,
                    "拆单笔数超过平台上限：" + limits.getMaxBillsPerBatch()
                            + "（当前=" + splits.size() + "，可提高单笔限额或拆分业务单）");
        }
        String encryptedUserName = paySensitiveCryptoHelper.encryptUserName(request.userName());
        try {
            PaymentTransferBatch batch = paymentTransferOrderService.createPendingBatch(
                    bizOrderNo,
                    provider.providerId(),
                    provider.payPlatform(),
                    request.openid().trim(),
                    encryptedUserName,
                    request.amountCents(),
                    splits,
                    request.transferSceneId().trim(),
                    request.transferRemark().trim());
            log.info(
                    "转账批次已创建（待调度派发）：batchNo={}, bizOrderNo={}, total={}, bills={}",
                    batch.getBatchNo(),
                    batch.getBizOrderNo(),
                    batch.getAmountTotalCents(),
                    batch.getBillCount());
            return getBatch(batch.getBatchNo());
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            // 并发双插命中 uk(biz_order_no, is_deleted) 时回读原批次
            Optional<PaymentTransferBatch> raced = paymentTransferOrderService.findByBizOrderNo(bizOrderNo);
            if (raced.isPresent()) {
                assertIdempotentMatch(raced.get(), request);
                log.info("转账创建并发幂等命中：bizOrderNo={}, batchNo={}", bizOrderNo, raced.get().getBatchNo());
                return getBatch(raced.get().getBatchNo());
            }
            throw ex;
        }
    }

    /**
     * 幂等命中时校验关键参数一致，避免静默返回旧批次掩盖改参重试。
     */
    private static void assertIdempotentMatch(PaymentTransferBatch existing, CreateTransferRequest request) {
        if (!java.util.Objects.equals(existing.getAmountTotalCents(), request.amountCents())) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_IDEMPOTENT_CONFLICT,
                    "转账业务单号已存在但金额不一致：" + existing.getBizOrderNo());
        }
        String requestOpenid = request.openid() == null ? null : request.openid().trim();
        if (!java.util.Objects.equals(existing.getOpenid(), requestOpenid)) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_IDEMPOTENT_CONFLICT,
                    "转账业务单号已存在但 openid 不一致：" + existing.getBizOrderNo());
        }
        String requestScene =
                request.transferSceneId() == null ? null : request.transferSceneId().trim();
        if (!java.util.Objects.equals(existing.getTransferSceneId(), requestScene)) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_IDEMPOTENT_CONFLICT,
                    "转账业务单号已存在但转账场景不一致：" + existing.getBizOrderNo());
        }
        String requestProvider = request.providerId() == null ? null : request.providerId().trim();
        if (StringUtils.hasText(requestProvider)
                && !java.util.Objects.equals(existing.getProviderId(), requestProvider)) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_IDEMPOTENT_CONFLICT,
                    "转账业务单号已存在但 Provider 不一致：" + existing.getBizOrderNo());
        }
    }

    /**
     * 查询转账批次及明细。
     *
     * @param batchNo 批次号
     * @return 批次视图
     */
    public TransferBatchVO getBatch(String batchNo) {
        PaymentTransferBatch batch = paymentTransferOrderService.requireBatch(batchNo);
        List<PaymentTransferBill> bills = paymentTransferOrderService.listBillsByBatch(batchNo);
        return toBatchVo(batch, bills);
    }

    /**
     * 分页查询转账批次；列表不含明细，明细请走 {@link #getBatch(String)}。
     *
     * @param query 分页与筛选
     * @return 批次分页
     */
    public PageVO<TransferBatchVO> pageBatches(TransferBatchPageQuery query) {
        Page<PaymentTransferBatch> page = paymentTransferOrderService.pageBatches(query);
        List<TransferBatchVO> records = page.getRecords().stream()
                .map(batch -> toBatchVo(batch, List.of()))
                .toList();
        return PageVO.of(records, page.getTotal(), query);
    }

    /**
     * 停止指定批次派发。
     *
     * @param batchNo    批次号
     * @param stopReason 停止原因
     * @return 批次视图
     */
    public TransferBatchVO stopBatch(String batchNo, String stopReason) {
        paymentTransferOrderService.stopBatch(
                batchNo, StringUtils.hasText(stopReason) ? stopReason : "人工停止");
        return getBatch(batchNo);
    }

    /**
     * 恢复指定批次派发，并触发一次全局派发。
     *
     * @param batchNo 批次号
     * @return 批次视图
     */
    public TransferBatchVO resumeBatch(String batchNo) {
        paymentTransferOrderService.resumeBatch(batchNo);
        transferDispatchService.dispatchPending();
        return getBatch(batchNo);
    }

    /**
     * 处理渠道转账回调。
     *
     * @param providerId Provider 标识
     * @param context    回调上下文
     */
    public void handleTransferNotify(String providerId, ChannelNotifyContext context) {
        TransferProvider provider = transferProviderManager.require(providerId);
        TransferNotifyResult notify = provider.parseTransferNotify(context);
        paymentTransferOrderService.applyChannelResult(
                notify.outBillNo(),
                notify.channelBillNo(),
                notify.targetStatus(),
                null,
                notify.amountCents(),
                notify.failReason(),
                notify.rawPayload());
    }

    /**
     * 主动查渠道并回写本地状态。
     *
     * @param outBillNo 平台转账单号
     * @return 明细视图
     */
    public TransferBillVO syncBillFromChannel(String outBillNo) {
        PaymentTransferBill bill = paymentTransferOrderService.requireBill(outBillNo);
        TransferProvider provider = transferProviderManager.require(bill.getProviderId());
        TransferQueryResult query = provider.queryTransfer(outBillNo);
        paymentTransferOrderService.applyChannelResult(
                firstNonBlank(query.outBillNo(), outBillNo),
                query.channelBillNo(),
                query.targetStatus(),
                query.packageInfo(),
                query.amountCents(),
                query.failReason(),
                query.rawPayload());
        return toBillVo(paymentTransferOrderService.requireBill(outBillNo));
    }

    /**
     * 按单笔限额拆分金额。
     *
     * @param totalCents  总金额
     * @param singleLimit 单笔限额
     * @return 拆单列表
     */
    static List<Long> splitAmount(long totalCents, long singleLimit) {
        if (totalCents <= 0 || singleLimit <= 0) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_AMOUNT_INVALID, "拆单金额不合法");
        }
        List<Long> parts = new ArrayList<>();
        long remain = totalCents;
        while (remain > 0) {
            long part = Math.min(remain, singleLimit);
            parts.add(part);
            remain -= part;
        }
        return parts;
    }

    private static TransferBatchVO toBatchVo(PaymentTransferBatch batch, List<PaymentTransferBill> bills) {
        return new TransferBatchVO(
                batch.getBatchNo(),
                batch.getBizOrderNo(),
                batch.getProviderId(),
                batch.getOpenid(),
                batch.getAmountTotalCents(),
                batch.getAmountSuccessCents(),
                batch.getStatus(),
                batch.getBillCount(),
                batch.getSuccessCount(),
                batch.getFailCount(),
                batch.getPendingCount(),
                batch.getTransferSceneId(),
                batch.getTransferRemark(),
                batch.getStopReason(),
                bills.stream().map(TransferService::toBillVo).toList(),
                batch.getCreateTime(),
                batch.getUpdateTime());
    }

    private static TransferBillVO toBillVo(PaymentTransferBill bill) {
        return new TransferBillVO(
                bill.getOutBillNo(),
                bill.getBatchNo(),
                bill.getSeqNo(),
                bill.getAmountCents(),
                bill.getStatus(),
                bill.getChannelBillNo(),
                bill.getPackageInfo(),
                bill.getFailReason(),
                bill.getDispatchedTime(),
                bill.getFinishedTime(),
                bill.getCreateTime());
    }

    private static String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }
}
