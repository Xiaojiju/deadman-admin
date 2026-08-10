package com.mtfm.deadman.plugin.pay.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBill;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBillVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 非终态转账单主动查单服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransferSyncService {

    private final TransferService transferService;
    private final PaymentTransferOrderService paymentTransferOrderService;
    private final PayPluginProperties payPluginProperties;

    /**
     * 扫描并同步非终态转账单。
     */
    public void syncPendingTransfers() {
        PayPluginProperties.TransferSync sync = payPluginProperties.getTransferSync();
        List<PaymentTransferBill> pending = paymentTransferOrderService.listNonTerminalForSync(
                sync.getMinAge(), sync.getMaxAge(), sync.getBatchSize());
        if (pending.isEmpty()) {
            log.debug("无待主动查单的转账单");
            return;
        }
        log.info("开始主动查转账，待处理数量={}", pending.size());
        for (PaymentTransferBill bill : pending) {
            try {
                TransferBillVO vo = transferService.syncBillFromChannel(bill.getOutBillNo());
                log.debug("主动查转账完成：outBillNo={}, status={}", vo.outBillNo(), vo.status());
            } catch (RuntimeException ex) {
                log.warn("主动查转账失败：outBillNo={}", bill.getOutBillNo(), ex);
            }
        }
    }
}
