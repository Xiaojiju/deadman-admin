package com.mtfm.deadman.plugin.pay.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingFinishRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingProvider;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReceiver;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnResult;

import lombok.RequiredArgsConstructor;

/**
 * 分账统一门面：合规校验后委托 {@link ProfitSharingProvider}。
 */
@Service
@RequiredArgsConstructor
public class ProfitSharingService {

    private final List<ProfitSharingProvider> providers;

    /**
     * 请求分账（硬性校验：仅 MERCHANT_ID，禁止 PERSONAL_OPENID）。
     *
     * @param providerId Provider 标识
     * @param request    请求
     * @param partnerMchid 服务商商户号（接收方必须等于该值）
     * @param orderAmountCents 订单总额（分），用于校验分账未覆盖全部货款
     * @return 结果
     */
    public ProfitSharingCreateResult create(
            String providerId,
            ProfitSharingCreateRequest request,
            String partnerMchid,
            int orderAmountCents) {
        validateReceivers(request.receivers(), partnerMchid, orderAmountCents);
        return requireProvider(providerId).create(request);
    }

    /**
     * 查询分账。
     *
     * @param providerId    Provider 标识
     * @param subMchid      二级商户号
     * @param transactionId 微信交易号
     * @param outOrderNo    平台分账单号
     * @return 查询结果
     */
    public ProfitSharingQueryResult query(
            String providerId, String subMchid, String transactionId, String outOrderNo) {
        return requireProvider(providerId).query(subMchid, transactionId, outOrderNo);
    }

    /**
     * 完结分账。
     *
     * @param providerId Provider 标识
     * @param request    请求
     * @return 结果
     */
    public ProfitSharingQueryResult finish(String providerId, ProfitSharingFinishRequest request) {
        return requireProvider(providerId).finish(request);
    }

    /**
     * 分账回退。
     *
     * @param providerId Provider 标识
     * @param request    请求
     * @return 结果
     */
    public ProfitSharingReturnResult returnOrder(String providerId, ProfitSharingReturnRequest request) {
        return requireProvider(providerId).returnOrder(request);
    }

    /**
     * 添加分账接收方。
     *
     * @param providerId   Provider 标识
     * @param appId        AppId
     * @param type         类型
     * @param account      商户号
     * @param relationType 关系
     */
    public void addReceiver(
            String providerId, String appId, String type, String account, String relationType) {
        if (!ProfitSharingReceiver.TYPE_MERCHANT_ID.equals(type)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分账接收方类型仅允许 MERCHANT_ID");
        }
        requireProvider(providerId).addReceiver(appId, type, account, relationType);
    }

    private ProfitSharingProvider requireProvider(String providerId) {
        if (providers == null || providers.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "未注册分账 Provider");
        }
        if (!StringUtils.hasText(providerId)) {
            return providers.getFirst();
        }
        return providers.stream()
                .filter(p -> p.supports(providerId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "未知分账 Provider: " + providerId));
    }

    private static void validateReceivers(
            List<ProfitSharingReceiver> receivers, String partnerMchid, int orderAmountCents) {
        if (receivers == null || receivers.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分账接收方不能为空");
        }
        if (!StringUtils.hasText(partnerMchid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "服务商商户号未配置");
        }
        int sum = 0;
        for (ProfitSharingReceiver receiver : receivers) {
            if (receiver == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "分账接收方无效");
            }
            if (!ProfitSharingReceiver.TYPE_MERCHANT_ID.equals(receiver.type())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "订单分账禁止使用 PERSONAL_OPENID，仅允许 MERCHANT_ID");
            }
            if (!partnerMchid.equals(receiver.account())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "分账接收方必须为服务商商户号");
            }
            if (receiver.amountCents() <= 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "分账金额必须大于 0");
            }
            sum += receiver.amountCents();
        }
        if (orderAmountCents > 0 && sum >= orderAmountCents) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分账合计不得覆盖全部货款，货款须留在二级商户");
        }
    }
}
