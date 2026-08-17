package com.mtfm.deadman.plugin.pay.controller;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.pay.dto.transfer.CreateTransferRequest;
import com.mtfm.deadman.plugin.pay.dto.transfer.TransferBatchPageQuery;
import com.mtfm.deadman.plugin.pay.dto.transfer.UpdateTransferQuotaRequest;
import com.mtfm.deadman.plugin.pay.facade.PayoutFacade;
import com.mtfm.deadman.plugin.pay.service.TransferDispatchService;
import com.mtfm.deadman.plugin.pay.service.TransferQuotaService;
import com.mtfm.deadman.plugin.pay.service.TransferService;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBatchVO;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferBillVO;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferQuotaVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 商家转账管理端 API：额度配置、发起转账、派发控制。
 */
@RestController
@RequestMapping("/api/pay/transfer")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransferAdminController {

    private final TransferQuotaService transferQuotaService;
    private final TransferService transferService;
    private final TransferDispatchService transferDispatchService;
    private final PayoutFacade payoutFacade;

    /**
     * 查询当前转账额度与占用。
     *
     * @return 额度视图
     */
    @GetMapping("/quota")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_QUOTA_READ)")
    public Result<TransferQuotaVO> getQuota() {
        return Result.ok(transferQuotaService.getQuota());
    }

    /**
     * 更新转账额度配置。
     *
     * @param request 更新请求
     * @return 更新后额度
     */
    @PutMapping("/quota")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_QUOTA_WRITE)")
    public Result<TransferQuotaVO> updateQuota(@Valid @RequestBody UpdateTransferQuotaRequest request) {
        return Result.ok(transferQuotaService.updateQuota(request));
    }

    /**
     * 人工停止全局待转派发。
     *
     * @return 额度视图
     */
    @PostMapping("/dispatch/pause")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_DISPATCH)")
    public Result<TransferQuotaVO> pauseDispatch() {
        return Result.ok(transferQuotaService.pauseDispatch());
    }

    /**
     * 恢复全局待转派发并立即尝试派发一轮。
     *
     * @return 额度视图与本轮派发笔数
     */
    @PostMapping("/dispatch/resume")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_DISPATCH)")
    public Result<Map<String, Object>> resumeDispatch() {
        TransferQuotaVO quota = transferQuotaService.resumeDispatch();
        int dispatched = transferDispatchService.dispatchPending();
        return Result.ok(Map.of("quota", quota, "dispatched", dispatched));
    }

    /**
     * 手动触发一轮待转派发。
     *
     * @return 本轮派发笔数
     */
    @PostMapping("/dispatch/run")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_DISPATCH)")
    public Result<Map<String, Integer>> runDispatch() {
        return Result.ok(Map.of("dispatched", transferDispatchService.dispatchPending()));
    }

    /**
     * 发起商家转账（超单笔自动拆单为待转；强制运营账户语义，经 {@link PayoutFacade}）。
     *
     * @param request 转账请求
     * @return 批次视图
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_CREATE)")
    public Result<TransferBatchVO> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        return Result.ok(payoutFacade.createPayout(request));
    }

    /**
     * 分页查询转账批次（不含明细）。
     *
     * @param query 分页与筛选
     * @return 批次分页
     */
    @GetMapping("/batches")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_READ)")
    public Result<PageVO<TransferBatchVO>> pageBatches(@Valid TransferBatchPageQuery query) {
        return Result.ok(transferService.pageBatches(query));
    }

    /**
     * 查询转账批次。
     *
     * @param batchNo 批次号
     * @return 批次视图
     */
    @GetMapping("/batches/{batchNo}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_READ)")
    public Result<TransferBatchVO> getBatch(@PathVariable String batchNo) {
        return Result.ok(payoutFacade.getBatch(batchNo));
    }

    /**
     * 停止指定批次的后续派发。
     *
     * @param batchNo 批次号
     * @param body    可选停止原因
     * @return 批次视图
     */
    @PostMapping("/batches/{batchNo}/stop")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_DISPATCH)")
    public Result<TransferBatchVO> stopBatch(
            @PathVariable String batchNo, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return Result.ok(transferService.stopBatch(batchNo, reason));
    }

    /**
     * 恢复指定批次并触发派发。
     *
     * @param batchNo 批次号
     * @return 批次视图
     */
    @PostMapping("/batches/{batchNo}/resume")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_DISPATCH)")
    public Result<TransferBatchVO> resumeBatch(@PathVariable String batchNo) {
        return Result.ok(transferService.resumeBatch(batchNo));
    }

    /**
     * 主动查渠道同步转账明细。
     *
     * @param outBillNo 平台转账单号
     * @return 明细视图
     */
    @PostMapping("/bills/{outBillNo}/sync")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.pay.permission.PayPermissions).TRANSFER_READ)")
    public Result<TransferBillVO> syncBill(@PathVariable String outBillNo) {
        return Result.ok(payoutFacade.syncBillFromChannel(outBillNo));
    }
}
