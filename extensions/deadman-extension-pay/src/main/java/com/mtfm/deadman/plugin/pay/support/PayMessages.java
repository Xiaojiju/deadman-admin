package com.mtfm.deadman.plugin.pay.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class PayMessages {

    private PayMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case PayErrorCodes.PAY_PROVIDER_NOT_FOUND -> ex(code, "result.PAY_PROVIDER_NOT_FOUND", "支付 Provider 不存在");
            case PayErrorCodes.PAY_ORDER_NOT_FOUND -> ex(code, "result.PAY_ORDER_NOT_FOUND", "支付单不存在");
            case PayErrorCodes.PAY_NOTIFY_PARSE_FAILED -> ex(code, "result.PAY_NOTIFY_PARSE_FAILED", "支付回调解析失败");
            case PayErrorCodes.PAY_QUERY_FAILED -> ex(code, "result.PAY_QUERY_FAILED", "支付查单失败");
            case PayErrorCodes.PAY_REFUND_PROVIDER_NOT_FOUND -> ex(code, "result.PAY_REFUND_PROVIDER_NOT_FOUND", "退款 Provider 不存在");
            case PayErrorCodes.PAY_REFUND_ORDER_NOT_FOUND -> ex(code, "result.PAY_REFUND_ORDER_NOT_FOUND", "退款单不存在");
            case PayErrorCodes.PAY_REFUND_NOT_ALLOWED -> ex(code, "result.PAY_REFUND_NOT_ALLOWED", "当前支付单不可退款");
            case PayErrorCodes.PAY_REFUND_AMOUNT_INVALID -> ex(code, "result.PAY_REFUND_AMOUNT_INVALID", "退款金额不合法");
            case PayErrorCodes.PAY_REFUND_FAILED -> ex(code, "result.PAY_REFUND_FAILED", "退款申请失败");
            case PayErrorCodes.PAY_REFUND_NOTIFY_PARSE_FAILED -> ex(code, "result.PAY_REFUND_NOTIFY_PARSE_FAILED", "退款回调解析失败");
            case PayErrorCodes.PAY_REFUND_QUERY_FAILED -> ex(code, "result.PAY_REFUND_QUERY_FAILED", "退款查单失败");
            case PayErrorCodes.PAY_REFUND_LIMIT_EXCEEDED -> ex(code, "result.PAY_REFUND_LIMIT_EXCEEDED", "退款次数已达上限");
            case PayErrorCodes.PAY_ABNORMAL_REFUND_FAILED -> ex(code, "result.PAY_ABNORMAL_REFUND_FAILED", "异常退款申请失败");
            case PayErrorCodes.PAY_ABNORMAL_REFUND_NOT_ALLOWED -> ex(code, "result.PAY_ABNORMAL_REFUND_NOT_ALLOWED", "当前退款单不可发起异常退款");
            case PayErrorCodes.PAY_TRANSFER_PROVIDER_NOT_FOUND -> ex(code, "result.PAY_TRANSFER_PROVIDER_NOT_FOUND", "转账 Provider 不存在");
            case PayErrorCodes.PAY_TRANSFER_BATCH_NOT_FOUND -> ex(code, "result.PAY_TRANSFER_BATCH_NOT_FOUND", "转账批次不存在");
            case PayErrorCodes.PAY_TRANSFER_BILL_NOT_FOUND -> ex(code, "result.PAY_TRANSFER_BILL_NOT_FOUND", "转账单不存在");
            case PayErrorCodes.PAY_TRANSFER_AMOUNT_INVALID -> ex(code, "result.PAY_TRANSFER_AMOUNT_INVALID", "转账金额不合法");
            case PayErrorCodes.PAY_TRANSFER_QUOTA_INVALID -> ex(code, "result.PAY_TRANSFER_QUOTA_INVALID", "转账额度配置不合法");
            case PayErrorCodes.PAY_TRANSFER_QUOTA_EXCEEDED -> ex(code, "result.PAY_TRANSFER_QUOTA_EXCEEDED", "转账额度不足");
            case PayErrorCodes.PAY_TRANSFER_DISPATCH_STOPPED -> ex(code, "result.PAY_TRANSFER_DISPATCH_STOPPED", "转账派发已停止");
            case PayErrorCodes.PAY_TRANSFER_FAILED -> ex(code, "result.PAY_TRANSFER_FAILED", "转账申请失败");
            case PayErrorCodes.PAY_TRANSFER_NOTIFY_PARSE_FAILED -> ex(code, "result.PAY_TRANSFER_NOTIFY_PARSE_FAILED", "转账回调解析失败");
            case PayErrorCodes.PAY_TRANSFER_AMOUNT_MISMATCH -> ex(code, "result.PAY_TRANSFER_AMOUNT_MISMATCH", "转账金额与本地不一致");
            case PayErrorCodes.PAY_TRANSFER_LIMIT_EXCEEDED -> ex(code, "result.PAY_TRANSFER_LIMIT_EXCEEDED", "转账创建超出平台限额");
            case PayErrorCodes.PAY_TRANSFER_STATUS_CONFLICT -> ex(code, "result.PAY_TRANSFER_STATUS_CONFLICT", "转账本地终态与渠道结果冲突");
            case PayErrorCodes.PAY_TRANSFER_IDEMPOTENT_CONFLICT -> ex(code, "result.PAY_TRANSFER_IDEMPOTENT_CONFLICT", "转账业务单号已存在但关键参数不一致");
            case PayErrorCodes.PAY_AMOUNT_MISMATCH -> ex(code, "result.PAY_AMOUNT_MISMATCH", "支付金额与本地不一致");
            case PayErrorCodes.PAY_REFUND_AMOUNT_MISMATCH -> ex(code, "result.PAY_REFUND_AMOUNT_MISMATCH", "退款金额或支付单号与本地不一致");
            case PayErrorCodes.PAY_FUND_LANE_MISMATCH -> ex(code, "result.PAY_FUND_LANE_MISMATCH", "支付资金链路与门面不匹配");
            case PayErrorCodes.PAY_FUND_ACCOUNT_MISMATCH -> ex(code, "result.PAY_FUND_ACCOUNT_MISMATCH", "资金账户与业务场景不匹配");
            case PayErrorCodes.PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND -> ex(code, "result.PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND", "二级商户 Provider 不存在");
            case PayErrorCodes.PAY_SUB_MERCHANT_APPLY_FAILED -> ex(code, "result.PAY_SUB_MERCHANT_APPLY_FAILED", "二级商户进件失败");
            case PayErrorCodes.PAY_MEDIA_UPLOAD_FAILED -> ex(code, "result.PAY_MEDIA_UPLOAD_FAILED", "支付渠道媒体文件上传失败");
            case PayErrorCodes.PAY_SCORE_PROVIDER_NOT_FOUND -> ex(code, "result.PAY_SCORE_PROVIDER_NOT_FOUND", "支付分 Provider 不存在");
            case PayErrorCodes.PAY_SCORE_NOTIFY_PARSE_FAILED -> ex(code, "result.PAY_SCORE_NOTIFY_PARSE_FAILED", "支付分回调解析失败");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
