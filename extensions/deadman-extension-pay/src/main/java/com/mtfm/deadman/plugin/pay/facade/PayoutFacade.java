package com.mtfm.deadman.plugin.pay.facade;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PlatformFundAccountType;
import com.mtfm.deadman.plugin.pay.constant.PlatformFundBizScene;
import com.mtfm.deadman.plugin.pay.dto.transfer.CreateTransferRequest;
import com.mtfm.deadman.plugin.pay.service.PlatformFundLedgerService;
import com.mtfm.deadman.plugin.pay.service.TransferService;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBatchVO;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBillVO;

import lombok.RequiredArgsConstructor;

/**
 * 运营打款门面：商家转账到零钱，出资账户强制为 {@link PlatformFundAccountType#OPERATION}。
 * <p>
 * 禁止从基本账户发起；交易佣金须先完成「BASIC 提现 → 对公 → OPERATION 充值」后再调用本门面。
 */
@Service
@RequiredArgsConstructor
public class PayoutFacade {

    private final TransferService transferService;
    private final PlatformFundLedgerService platformFundLedgerService;

    /**
     * 发起商家转账（落 PENDING，由调度派发；强制运营账户语义）。
     *
     * @param request 转账请求
     * @return 批次视图
     */
    public TransferBatchVO createPayout(CreateTransferRequest request) {
        PlatformFundLedgerService.assertAccount(
                PlatformFundBizScene.PAYOUT_TO_WALLET, PlatformFundAccountType.OPERATION);
        if (request == null || !StringUtils.hasText(request.bizOrderNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "打款业务单号不能为空");
        }
        return transferService.createTransfer(request);
    }

    /**
     * 查询转账批次。
     *
     * @param batchNo 批次号
     * @return 批次视图
     */
    public TransferBatchVO getBatch(String batchNo) {
        return transferService.getBatch(batchNo);
    }

    /**
     * 处理转账回调。
     *
     * @param providerId Provider
     * @param context    回调上下文
     */
    public void handleTransferNotify(String providerId, ChannelNotifyContext context) {
        transferService.handleTransferNotify(providerId, context);
    }

    /**
     * 主动查单同步。
     *
     * @param outBillNo 平台转账单号
     * @return 明细视图
     */
    public TransferBillVO syncBillFromChannel(String outBillNo) {
        return transferService.syncBillFromChannel(outBillNo);
    }

    /**
     * 记录运营账户打款出账流水（幂等）。
     * <p>
     * 明细转入 SUCCESS 时由 {@link PaymentTransferOrderService} 自动记账；本方法供人工补记或对账。
     *
     * @param amountCents   金额（分）
     * @param payPlatform   支付平台
     * @param bizOrderNo    业务单号
     * @param channelRefNo  渠道单号
     * @param remark        备注
     * @param idempotentKey 幂等键
     * @return 流水号
     */
    public String recordPayoutLedger(
            long amountCents,
            String payPlatform,
            String bizOrderNo,
            String channelRefNo,
            String remark,
            String idempotentKey) {
        return platformFundLedgerService.record(
                PlatformFundBizScene.PAYOUT_TO_WALLET,
                amountCents,
                payPlatform,
                bizOrderNo,
                channelRefNo,
                remark,
                idempotentKey);
    }
}
