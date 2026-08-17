package com.mtfm.deadman.plugin.pay.spi.merchant;

/**
 * 二级商户进件渠道扩展参数键（写入 {@link SubMerchantApplyCommand#channelParams()}）。
 * <p>
 * 键名对齐微信收付通「提交申请单」字段；仅收录当前实现会用到的参数。
 * 条件必填规则见各常量注释及微信文档。
 *
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012713017">提交申请单</a>
 */
public final class SubMerchantChannelParams {

    // ---------- account_info（始终必填） ----------

    /** 账户类型：74对公 / 75对私 */
    public static final String BANK_ACCOUNT_TYPE = "bankAccountType";

    /** 开户银行 */
    public static final String ACCOUNT_BANK = "accountBank";

    /** 开户名称（敏感） */
    public static final String ACCOUNT_NAME = "accountName";

    /** 银行账号（敏感） */
    public static final String ACCOUNT_NUMBER = "accountNumber";

    // ---------- contact_info（始终必填子集） ----------

    /** 超管类型：65经营者/法定代表人，66经办人 */
    public static final String CONTACT_TYPE = "contactType";

    /** 超管姓名（敏感） */
    public static final String CONTACT_NAME = "contactName";

    /** 超管手机（敏感） */
    public static final String MOBILE_PHONE = "mobilePhone";

    /**
     * 超管证件号（敏感，选填；法定代表人时建议与法人身份证一致）。
     */
    public static final String CONTACT_ID_CARD_NUMBER = "contactIdCardNumber";

    // ---------- sales_scene_info ----------

    /** 店铺名称（必填） */
    public static final String STORE_NAME = "storeName";

    /** 店铺链接（与 STORE_QR_CODE 二选一） */
    public static final String STORE_URL = "storeUrl";

    /** 店铺二维码 MediaID（与 STORE_URL 二选一） */
    public static final String STORE_QR_CODE = "storeQrCode";

    // ---------- business_license_info（个体户/企业等有执照主体必填） ----------

    /** 营业执照/登记证书扫描件 MediaID */
    public static final String BUSINESS_LICENSE_COPY = "businessLicenseCopy";

    /** 营业执照注册号/统一社会信用代码 */
    public static final String BUSINESS_LICENSE_NUMBER = "businessLicenseNumber";

    /** 执照上的商户名称 */
    public static final String BUSINESS_LICENSE_MERCHANT_NAME = "businessLicenseMerchantName";

    /** 经营者/法定代表人姓名（执照） */
    public static final String BUSINESS_LICENSE_LEGAL_PERSON = "businessLicenseLegalPerson";

    // ---------- id_card_info（证件类型为大陆身份证时） ----------

    /** 身份证人像面 MediaID */
    public static final String ID_CARD_COPY = "idCardCopy";

    /** 身份证国徽面 MediaID */
    public static final String ID_CARD_NATIONAL = "idCardNational";

    /** 身份证姓名（敏感） */
    public static final String ID_CARD_NAME = "idCardName";

    /** 身份证号码（敏感） */
    public static final String ID_CARD_NUMBER = "idCardNumber";

    /** 身份证有效期开始 YYYY-MM-DD */
    public static final String ID_CARD_VALID_TIME_BEGIN = "idCardValidTimeBegin";

    /** 身份证有效期结束 YYYY-MM-DD 或「长期」 */
    public static final String ID_CARD_VALID_TIME = "idCardValidTime";

    private SubMerchantChannelParams() {
    }
}
