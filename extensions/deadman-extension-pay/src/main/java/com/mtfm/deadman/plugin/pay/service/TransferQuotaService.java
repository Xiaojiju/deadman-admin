package com.mtfm.deadman.plugin.pay.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.MerchantEntityType;
import com.mtfm.deadman.plugin.pay.constant.TransferQuotaDefaults;
import com.mtfm.deadman.plugin.pay.dto.transfer.UpdateTransferQuotaRequest;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferQuota;
import com.mtfm.deadman.plugin.pay.mapper.PaymentTransferBillMapper;
import com.mtfm.deadman.plugin.pay.mapper.PaymentTransferQuotaMapper;
import com.mtfm.deadman.plugin.pay.vo.transfer.TransferQuotaVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 商家转账额度配置服务（API 维护，非 yaml）。
 * <p>
 * 运行时仅维护<strong>单行</strong>当前配置；切换主体类型时更新同一行，不新增行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferQuotaService {

    /** 与微信商户日切对齐的时区 */
    public static final ZoneId QUOTA_ZONE = ZoneId.of("Asia/Shanghai");

    private final PaymentTransferQuotaMapper transferQuotaMapper;
    private final PaymentTransferBillMapper transferBillMapper;

    /**
     * 额度闸门细分结果（用于派发 continue/break）。
     */
    public enum QuotaGate {
        /** 可通过 */
        OK,
        /** 单用户日额不足 */
        USER_DAILY_EXCEEDED,
        /** 单日总额不足 */
        DAILY_EXCEEDED,
        /** 单月总额不足 */
        MONTHLY_EXCEEDED
    }

    /**
     * 查询当前额度配置与日/月占用。
     *
     * @return 额度视图
     */
    public TransferQuotaVO getQuota() {
        PaymentTransferQuota quota = requireCurrent();
        LocalDateTime dayStart = todayStart();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime monthStart = monthStart();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        long usedDaily = transferBillMapper.sumQuotaAmount(dayStart, dayEnd);
        long usedMonthly = transferBillMapper.sumQuotaAmount(monthStart, monthEnd);
        return toVo(quota, usedDaily, usedMonthly);
    }

    /**
     * 更新额度配置（单笔/单用户日/单日可调；月额度随主体类型固定）。
     *
     * @param request 更新请求
     * @return 更新后额度视图
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferQuotaVO updateQuota(UpdateTransferQuotaRequest request) {
        String entityType = normalizeEntityType(request.merchantEntityType());
        validateAdjustableLimits(entityType, request);
        PaymentTransferQuota quota = requireCurrent();
        long monthly = MerchantEntityType.INDIVIDUAL.equals(entityType)
                ? TransferQuotaDefaults.INDIVIDUAL_MONTHLY
                : TransferQuotaDefaults.NON_INDIVIDUAL_MONTHLY;
        quota.setMerchantEntityType(entityType);
        quota.setSingleLimitCents(request.singleLimitCents());
        quota.setUserDailyLimitCents(request.userDailyLimitCents());
        quota.setDailyLimitCents(request.dailyLimitCents());
        quota.setMonthlyLimitCents(monthly);
        transferQuotaMapper.updateById(quota);
        return getQuota();
    }

    /**
     * 人工停止待转派发。
     *
     * @return 更新后额度视图
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferQuotaVO pauseDispatch() {
        PaymentTransferQuota quota = requireCurrent();
        quota.setDispatchEnabled(0);
        transferQuotaMapper.updateById(quota);
        return getQuota();
    }

    /**
     * 恢复待转派发。
     *
     * @return 更新后额度视图
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferQuotaVO resumeDispatch() {
        PaymentTransferQuota quota = requireCurrent();
        quota.setDispatchEnabled(1);
        transferQuotaMapper.updateById(quota);
        return getQuota();
    }

    /**
     * 当前是否允许派发。
     *
     * @return 允许时返回 true
     */
    public boolean isDispatchEnabled() {
        PaymentTransferQuota quota = requireCurrent();
        return quota.getDispatchEnabled() != null && quota.getDispatchEnabled() == 1;
    }

    /**
     * 加载当前额度配置（不存在则按非个体户默认值初始化）。
     * <p>
     * 若历史数据存在多行，仅保留 id 最小一行，其余逻辑删除，保证「单行 current」语义。
     *
     * @return 额度实体
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransferQuota requireCurrent() {
        List<PaymentTransferQuota> rows = transferQuotaMapper.selectList(new LambdaQueryWrapper<PaymentTransferQuota>()
                .orderByAsc(PaymentTransferQuota::getId));
        if (rows == null || rows.isEmpty()) {
            PaymentTransferQuota quota = PaymentTransferQuota.builder()
                    .merchantEntityType(MerchantEntityType.NON_INDIVIDUAL)
                    .singleLimitCents(TransferQuotaDefaults.SINGLE_DEFAULT)
                    .userDailyLimitCents(TransferQuotaDefaults.NON_INDIVIDUAL_USER_DAILY_DEFAULT)
                    .dailyLimitCents(TransferQuotaDefaults.NON_INDIVIDUAL_DAILY_DEFAULT)
                    .monthlyLimitCents(TransferQuotaDefaults.NON_INDIVIDUAL_MONTHLY)
                    .dispatchEnabled(1)
                    .build();
            transferQuotaMapper.insert(quota);
            return quota;
        }
        PaymentTransferQuota current = rows.getFirst();
        if (rows.size() > 1) {
            log.warn(
                    "转账额度配置存在 {} 行，仅保留 id={} 作为当前配置，其余逻辑删除",
                    rows.size(),
                    current.getId());
            for (int i = 1; i < rows.size(); i++) {
                transferQuotaMapper.deleteById(rows.get(i).getId());
            }
        }
        return current;
    }

    /**
     * 在已持有额度行锁的前提下评估闸门（供派发抢占事务内调用）。
     *
     * @param openid      收款人
     * @param amountCents 本笔金额
     * @param quota       已锁定的额度配置
     * @return 闸门结果
     */
    public QuotaGate evaluateQuotaGate(String openid, long amountCents, PaymentTransferQuota quota) {
        LocalDateTime dayStart = todayStart();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime monthStart = monthStart();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        long usedUserDaily = transferBillMapper.sumUserQuotaAmount(openid, dayStart, dayEnd);
        if (usedUserDaily + amountCents > quota.getUserDailyLimitCents()) {
            return QuotaGate.USER_DAILY_EXCEEDED;
        }
        long usedDaily = transferBillMapper.sumQuotaAmount(dayStart, dayEnd);
        if (usedDaily + amountCents > quota.getDailyLimitCents()) {
            return QuotaGate.DAILY_EXCEEDED;
        }
        long usedMonthly = transferBillMapper.sumQuotaAmount(monthStart, monthEnd);
        if (usedMonthly + amountCents > quota.getMonthlyLimitCents()) {
            return QuotaGate.MONTHLY_EXCEEDED;
        }
        return QuotaGate.OK;
    }

    /**
     * 校验单笔金额是否可占用额度（用户日/日/月）。
     *
     * @param openid      收款人
     * @param amountCents 本笔金额
     * @param quota       额度配置
     * @return 可派发时返回 true
     */
    public boolean canOccupyQuota(String openid, long amountCents, PaymentTransferQuota quota) {
        return evaluateQuotaGate(openid, amountCents, quota) == QuotaGate.OK;
    }

    /**
     * 东八区今日 0 点。
     *
     * @return 日切起点
     */
    public static LocalDateTime todayStart() {
        return LocalDate.now(QUOTA_ZONE).atStartOfDay();
    }

    /**
     * 东八区本月 1 日 0 点。
     *
     * @return 月切起点
     */
    public static LocalDateTime monthStart() {
        return YearMonth.now(QUOTA_ZONE).atDay(1).atStartOfDay();
    }

    private static String normalizeEntityType(String merchantEntityType) {
        if (!StringUtils.hasText(merchantEntityType)) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_QUOTA_INVALID, "商户主体类型不能为空");
        }
        String normalized = merchantEntityType.trim().toUpperCase();
        if (!MerchantEntityType.NON_INDIVIDUAL.equals(normalized)
                && !MerchantEntityType.INDIVIDUAL.equals(normalized)) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_QUOTA_INVALID, "商户主体类型无效：" + merchantEntityType);
        }
        return normalized;
    }

    private static void validateAdjustableLimits(String entityType, UpdateTransferQuotaRequest request) {
        if (request.singleLimitCents() < TransferQuotaDefaults.SINGLE_MIN
                || request.singleLimitCents() > TransferQuotaDefaults.SINGLE_MAX) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_QUOTA_INVALID,
                    "单笔限额须在 " + TransferQuotaDefaults.SINGLE_MIN + "~" + TransferQuotaDefaults.SINGLE_MAX + " 分");
        }
        long userDailyMax = MerchantEntityType.INDIVIDUAL.equals(entityType)
                ? TransferQuotaDefaults.INDIVIDUAL_USER_DAILY_MAX
                : TransferQuotaDefaults.NON_INDIVIDUAL_USER_DAILY_MAX;
        long dailyMax = MerchantEntityType.INDIVIDUAL.equals(entityType)
                ? TransferQuotaDefaults.INDIVIDUAL_DAILY_MAX
                : TransferQuotaDefaults.NON_INDIVIDUAL_DAILY_MAX;
        if (request.userDailyLimitCents() < TransferQuotaDefaults.SINGLE_MIN
                || request.userDailyLimitCents() > userDailyMax) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_QUOTA_INVALID,
                    "单用户日限额超出可调范围：" + TransferQuotaDefaults.SINGLE_MIN + "~" + userDailyMax);
        }
        if (request.dailyLimitCents() < TransferQuotaDefaults.SINGLE_MIN || request.dailyLimitCents() > dailyMax) {
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_QUOTA_INVALID,
                    "单日总额度超出可调范围：" + TransferQuotaDefaults.SINGLE_MIN + "~" + dailyMax);
        }
        if (request.singleLimitCents() > request.userDailyLimitCents()) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_QUOTA_INVALID, "单笔限额不能大于单用户日限额");
        }
        if (request.userDailyLimitCents() > request.dailyLimitCents()) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_QUOTA_INVALID, "单用户日限额不能大于单日总额度");
        }
    }

    private static TransferQuotaVO toVo(PaymentTransferQuota quota, long usedDaily, long usedMonthly) {
        boolean individual = MerchantEntityType.INDIVIDUAL.equals(quota.getMerchantEntityType());
        return new TransferQuotaVO(
                quota.getMerchantEntityType(),
                quota.getSingleLimitCents(),
                quota.getUserDailyLimitCents(),
                quota.getDailyLimitCents(),
                quota.getMonthlyLimitCents(),
                quota.getDispatchEnabled() != null && quota.getDispatchEnabled() == 1,
                usedDaily,
                usedMonthly,
                TransferQuotaDefaults.SINGLE_MIN,
                TransferQuotaDefaults.SINGLE_MAX,
                individual
                        ? TransferQuotaDefaults.INDIVIDUAL_USER_DAILY_MAX
                        : TransferQuotaDefaults.NON_INDIVIDUAL_USER_DAILY_MAX,
                individual ? TransferQuotaDefaults.INDIVIDUAL_DAILY_MAX : TransferQuotaDefaults.NON_INDIVIDUAL_DAILY_MAX);
    }
}
