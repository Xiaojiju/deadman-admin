package com.mtfm.deadman.plugin.pay.wechat.util;

import java.util.Locale;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.wechat.pay.java.core.exception.ServiceException;

/**
 * 微信渠道错误分类：区分「明确业务拒绝」与「不确定是否已受理」。
 * <p>
 * 资金安全原则：未列入明确拒绝白名单的 {@link ServiceException} 一律按不确定处理，
 * 上层应保留 PROCESSING 并查单，避免误关单/误 FAIL 导致双退或双付。
 */
public final class WechatPayChannelErrorClassifier {

    /**
     * 微信已明确拒绝本笔请求、可安全视为未受理的错误码（参数/权限/余额等）。
     * 不含 SYSTEM_ERROR、FREQUENCY_LIMITED、ALREADY_EXISTS 等需原单重试或查单的错误。
     */
    private static final Set<String> CLEAR_REJECT_ERROR_CODES = Set.of(
            "PARAM_ERROR",
            "INVALID_REQUEST",
            "APPID_MCHID_NOT_MATCH",
            "MCH_NOT_EXISTS",
            "NO_AUTH",
            "SIGN_ERROR",
            "NOT_ENOUGH",
            "ACCOUNT_ERROR",
            "OPENID_MISMATCH",
            "NAME_MISMATCH",
            "RULE_LIMIT",
            "INVALID_TRANSACTIONID",
            "ORDER_NOT_EXIST",
            "REFUND_NOT_EXISTS",
            "USER_ACCOUNT_ABNORMAL",
            "CONTRACT_NOT_EXIST");

    private WechatPayChannelErrorClassifier() {
    }

    /**
     * 是否为渠道明确业务拒绝（可关单/落 FAIL）。
     *
     * @param ex 微信 ServiceException
     * @return true 表示明确未受理
     */
    public static boolean isClearReject(ServiceException ex) {
        if (ex == null) {
            return false;
        }
        String code = normalize(ex.getErrorCode());
        return StringUtils.hasText(code) && CLEAR_REJECT_ERROR_CODES.contains(code);
    }

    /**
     * 是否为不确定结果（超时/系统错误/限频/已存在等，应查单）。
     *
     * @param ex 微信 ServiceException
     * @return true 表示结果不确定
     */
    public static boolean isUncertain(ServiceException ex) {
        return !isClearReject(ex);
    }

    private static String normalize(String errorCode) {
        if (!StringUtils.hasText(errorCode)) {
            return "";
        }
        return errorCode.trim().toUpperCase(Locale.ROOT);
    }
}
