package com.mtfm.deadman.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务响应码定义，可按模块扩展。
 */
@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未认证"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源冲突"),
    INTERNAL_ERROR(50000, "系统内部错误"),

    USER_NOT_FOUND(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已禁用"),
    ACCOUNT_EXISTS(10003, "账号已存在"),
    ACCOUNT_NOT_FOUND(10004, "账号不存在"),
    PASSWORD_MISMATCH(10005, "用户名或密码错误"),
    PASSWORD_NOT_SET(10006, "未设置密码"),
    USER_SUPER_ADMIN_PROTECTED(10007, "超级管理员用户不允许删除或停用"),
    TOKEN_INVALID(10008, "令牌无效或已过期"),
    TOKEN_REUSE_DETECTED(10009, "检测到令牌异常重用，请重新登录"),

    ROLE_NOT_FOUND(11001, "角色不存在"),
    ROLE_CODE_EXISTS(11002, "角色编码已存在"),
    ROLE_SYSTEM_PROTECTED(11003, "系统内置角色不允许删除或禁用"),
    ROLE_SUPER_ADMIN_PROTECTED(11004, "超级管理员角色不允许修改权限"),
    PERMISSION_INVALID(11005, "存在无效的权限码"),

    DEPARTMENT_NOT_FOUND(12001, "部门不存在"),
    DEPARTMENT_CODE_EXISTS(12002, "部门编码已存在"),
    DEPARTMENT_HAS_CHILDREN(12003, "存在下级部门，无法删除"),
    DEPARTMENT_HAS_USERS(12004, "部门下存在用户，无法删除"),
    POSITION_NOT_FOUND(12011, "职位不存在"),
    POSITION_CODE_EXISTS(12012, "职位编码已存在"),
    POSITION_HAS_USERS(12013, "职位下存在用户，无法删除"),
    POSITION_DEPT_MISMATCH(12014, "职位所属部门与用户部门不一致"),
    PHONE_EXISTS(12021, "手机号已被其他用户绑定"),

    OAUTH_ALREADY_BOUND(12031, "该第三方账号已被其他用户绑定"),
    WECHAT_BIND_TOKEN_INVALID(12032, "微信绑定临时令牌无效或已过期"),

    FILE_NOT_FOUND(13001, "文件不存在"),
    FILE_TOO_LARGE(13002, "文件大小超出限制"),
    FILE_STORAGE_ERROR(13003, "文件存储失败"),
    FILE_PROVIDER_NOT_FOUND(13004, "文件存储 Provider 不存在"),
    FILE_BIZ_TYPE_UNREGISTERED(13005, "文件业务分类未注册"),

    WECHAT_PAY_ORDER_NOT_FOUND(14001, "支付单不存在"),
    WECHAT_PAY_OPENID_REQUIRED(14002, "缺少付款人 openid"),
    WECHAT_PAY_PREPAY_FAILED(14003, "微信预下单失败"),
    WECHAT_PAY_CONFIG_INVALID(14004, "微信支付配置无效"),
    WECHAT_PAY_REFUND_FAILED(14005, "微信退款申请失败"),
    WECHAT_PAY_ABNORMAL_REFUND_FAILED(14006, "微信异常退款申请失败"),

    PAY_PROVIDER_NOT_FOUND(14101, "支付 Provider 不存在"),
    PAY_ORDER_NOT_FOUND(14102, "支付单不存在"),
    PAY_NOTIFY_PARSE_FAILED(14103, "支付回调解析失败"),
    PAY_QUERY_FAILED(14104, "支付查单失败"),
    PAY_REFUND_PROVIDER_NOT_FOUND(14105, "退款 Provider 不存在"),
    PAY_REFUND_ORDER_NOT_FOUND(14106, "退款单不存在"),
    PAY_REFUND_NOT_ALLOWED(14107, "当前支付单不可退款"),
    PAY_REFUND_AMOUNT_INVALID(14108, "退款金额不合法"),
    PAY_REFUND_FAILED(14109, "退款申请失败"),
    PAY_REFUND_NOTIFY_PARSE_FAILED(14110, "退款回调解析失败"),
    PAY_REFUND_QUERY_FAILED(14111, "退款查单失败"),
    PAY_REFUND_LIMIT_EXCEEDED(14112, "退款次数已达上限"),
    PAY_ABNORMAL_REFUND_FAILED(14113, "异常退款申请失败"),
    PAY_ABNORMAL_REFUND_NOT_ALLOWED(14114, "当前退款单不可发起异常退款"),
    WECHAT_PAY_TRANSFER_FAILED(14007, "微信商家转账失败"),
    WECHAT_PAY_SCORE_FAILED(14008, "微信支付分调用失败"),
    PAY_TRANSFER_PROVIDER_NOT_FOUND(14115, "转账 Provider 不存在"),
    PAY_TRANSFER_BATCH_NOT_FOUND(14116, "转账批次不存在"),
    PAY_TRANSFER_BILL_NOT_FOUND(14117, "转账单不存在"),
    PAY_TRANSFER_AMOUNT_INVALID(14118, "转账金额不合法"),
    PAY_TRANSFER_QUOTA_INVALID(14119, "转账额度配置不合法"),
    PAY_TRANSFER_QUOTA_EXCEEDED(14120, "转账额度不足"),
    PAY_TRANSFER_DISPATCH_STOPPED(14121, "转账派发已停止"),
    PAY_TRANSFER_FAILED(14122, "转账申请失败"),
    PAY_TRANSFER_NOTIFY_PARSE_FAILED(14123, "转账回调解析失败"),
    PAY_TRANSFER_AMOUNT_MISMATCH(14124, "转账金额与本地不一致"),
    PAY_TRANSFER_LIMIT_EXCEEDED(14125, "转账创建超出平台限额"),
    PAY_TRANSFER_STATUS_CONFLICT(14126, "转账本地终态与渠道结果冲突"),
    PAY_TRANSFER_IDEMPOTENT_CONFLICT(14127, "转账业务单号已存在但关键参数不一致"),
    PAY_AMOUNT_MISMATCH(14128, "支付金额与本地不一致"),
    PAY_REFUND_AMOUNT_MISMATCH(14129, "退款金额或支付单号与本地不一致"),
    PAY_FUND_LANE_MISMATCH(14130, "支付资金链路与门面不匹配"),
    PAY_FUND_ACCOUNT_MISMATCH(14131, "资金账户与业务场景不匹配"),
    PAY_SUB_MERCHANT_PROVIDER_NOT_FOUND(14132, "二级商户 Provider 不存在"),
    PAY_SUB_MERCHANT_APPLY_FAILED(14133, "二级商户进件失败"),
    PAY_MEDIA_UPLOAD_FAILED(14134, "支付渠道媒体文件上传失败"),
    PAY_SCORE_PROVIDER_NOT_FOUND(14135, "支付分 Provider 不存在"),
    PAY_SCORE_NOTIFY_PARSE_FAILED(14136, "支付分回调解析失败"),

    CRYPTO_CONFIG_INVALID(14401, "加解密配置无效"),
    CRYPTO_KEY_NOT_FOUND(14402, "加解密密钥不存在"),
    CRYPTO_ALGORITHM_UNSUPPORTED(14403, "不支持的加解密算法"),
    CRYPTO_ENCRYPT_FAILED(14404, "加密失败"),
    CRYPTO_DECRYPT_FAILED(14405, "解密失败"),
    CRYPTO_PAYLOAD_INVALID(14406, "密文格式无效"),

    LOGISTICS_PROVIDER_NOT_FOUND(14201, "物流 Provider 不存在"),
    LOGISTICS_TRACK_QUERY_FAILED(14202, "快递轨迹查询失败"),
    LOGISTICS_CONFIG_INVALID(14203, "物流插件配置无效"),
    LOGISTICS_CARRIER_DETECT_FAILED(14204, "快递公司识别失败"),
    LOGISTICS_SUBSCRIBE_FAILED(14205, "快递轨迹订阅失败"),
    LOGISTICS_SUBSCRIBE_PUSH_INVALID(14206, "订阅推送验签失败"),
    LOGISTICS_WAYBILL_FAILED(14207, "电子面单操作失败"),
    LOGISTICS_SHIP_ORDER_FAILED(14208, "寄件下单失败"),
    LOGISTICS_SHIP_CANCEL_FAILED(14209, "寄件取消失败"),
    LOGISTICS_CARRIER_CODE_UNKNOWN(14210, "快递公司编码未注册或不支持当前渠道"),

    IM_CONFIG_INVALID(14301, "IM 插件配置无效"),
    IM_REALM_UNKNOWN(14302, "IM 用户域未注册"),
    IM_USER_DISABLED(14303, "IM 用户已禁用"),
    IM_ACCOUNT_SYNC_FAILED(14304, "IM 账号同步失败"),
    IM_USER_NOT_FOUND(14305, "IM 用户映射不存在"),

    ESS_CONFIG_INVALID(14501, "电子签插件配置无效"),
    ESS_API_FAILED(14502, "电子签 API 调用失败"),
    ESS_UPLOAD_FAILED(14503, "电子签文件上传失败"),
    ESS_CALLBACK_INVALID(14504, "电子签回调验签或解密失败");

    private final int code;
    private final String message;
}
