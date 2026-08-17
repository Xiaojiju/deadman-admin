package com.mtfm.deadman.plugin.pay.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PlatformFundAccountType;
import com.mtfm.deadman.plugin.pay.constant.PlatformFundBizScene;
import com.mtfm.deadman.plugin.pay.entity.PluginPayBasicAccountFlow;
import com.mtfm.deadman.plugin.pay.entity.PluginPayOperateAccountFlow;
import com.mtfm.deadman.plugin.pay.mapper.PluginPayBasicAccountFlowMapper;
import com.mtfm.deadman.plugin.pay.mapper.PluginPayOperateAccountFlowMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 平台双账户本地账本：BASIC / OPERATION 物理分表，场景与账户硬绑定。
 * <p>
 * 不提供账户间转账接口；合规中转只能记「BASIC 提现出账」+「OPERATION 充值入账」两笔独立流水。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformFundLedgerService {

    private final PluginPayBasicAccountFlowMapper basicAccountFlowMapper;
    private final PluginPayOperateAccountFlowMapper operateAccountFlowMapper;

    /**
     * 记录资金流水（幂等）。
     *
     * @param scene         业务场景（决定账户与方向）
     * @param amountCents   金额（分）
     * @param payPlatform   支付平台
     * @param bizOrderNo    业务单号
     * @param channelRefNo  渠道单号
     * @param remark        备注
     * @param idempotentKey 幂等键
     * @return 流水号；幂等命中返回已有流水号
     */
    @Transactional(rollbackFor = Exception.class)
    public String record(
            PlatformFundBizScene scene,
            long amountCents,
            String payPlatform,
            String bizOrderNo,
            String channelRefNo,
            String remark,
            String idempotentKey) {
        if (scene == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资金场景不能为空");
        }
        if (amountCents <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资金流水金额必须大于 0");
        }
        if (!StringUtils.hasText(idempotentKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资金流水缺少幂等键");
        }
        String key = idempotentKey.trim();
        String existing = findExistingFlowNo(scene.accountType(), key);
        if (existing != null) {
            return existing;
        }
        String flowNo = "PF" + IdWorker.getId();
        try {
            if (PlatformFundAccountType.BASIC.equals(scene.accountType())) {
                basicAccountFlowMapper.insert(PluginPayBasicAccountFlow.builder()
                        .flowNo(flowNo)
                        .bizScene(scene.name())
                        .direction(scene.direction())
                        .amountCents(amountCents)
                        .payPlatform(payPlatform)
                        .bizOrderNo(bizOrderNo)
                        .channelRefNo(channelRefNo)
                        .remark(remark)
                        .idempotentKey(key)
                        .build());
            } else if (PlatformFundAccountType.OPERATION.equals(scene.accountType())) {
                operateAccountFlowMapper.insert(PluginPayOperateAccountFlow.builder()
                        .flowNo(flowNo)
                        .bizScene(scene.name())
                        .direction(scene.direction())
                        .amountCents(amountCents)
                        .payPlatform(payPlatform)
                        .bizOrderNo(bizOrderNo)
                        .channelRefNo(channelRefNo)
                        .remark(remark)
                        .idempotentKey(key)
                        .build());
            } else {
                throw new BusinessException(ResultCode.PAY_FUND_ACCOUNT_MISMATCH, "未知资金账户类型");
            }
        } catch (DuplicateKeyException ex) {
            String raced = findExistingFlowNo(scene.accountType(), key);
            if (raced != null) {
                return raced;
            }
            throw ex;
        }
        log.info(
                "已记账平台资金流水：account={}, scene={}, amount={}, flowNo={}, bizOrderNo={}",
                scene.accountType(),
                scene.name(),
                amountCents,
                flowNo,
                bizOrderNo);
        return flowNo;
    }

    /**
     * 断言场景必须落在指定账户（供门面调用前校验）。
     *
     * @param scene             场景
     * @param expectedAccount   期望账户
     */
    public static void assertAccount(PlatformFundBizScene scene, String expectedAccount) {
        if (scene == null || !expectedAccount.equals(scene.accountType())) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_ACCOUNT_MISMATCH,
                    "资金场景与账户不匹配：scene=" + scene + ", expected=" + expectedAccount);
        }
    }

    private String findExistingFlowNo(String accountType, String idempotentKey) {
        if (PlatformFundAccountType.BASIC.equals(accountType)) {
            PluginPayBasicAccountFlow flow = basicAccountFlowMapper.selectOne(new LambdaQueryWrapper<PluginPayBasicAccountFlow>()
                    .eq(PluginPayBasicAccountFlow::getIdempotentKey, idempotentKey)
                    .last("LIMIT 1"));
            return flow == null ? null : flow.getFlowNo();
        }
        PluginPayOperateAccountFlow flow = operateAccountFlowMapper.selectOne(new LambdaQueryWrapper<PluginPayOperateAccountFlow>()
                .eq(PluginPayOperateAccountFlow::getIdempotentKey, idempotentKey)
                .last("LIMIT 1"));
        return flow == null ? null : flow.getFlowNo();
    }
}
