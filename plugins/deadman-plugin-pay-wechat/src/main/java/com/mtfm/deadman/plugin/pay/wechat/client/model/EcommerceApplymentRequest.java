package com.mtfm.deadman.plugin.pay.wechat.client.model;

/**
 * 收付通二级商户进件请求体（对齐微信「提交申请单」实用子集）。
 * <p>
 * 字段命名配合 SDK {@code GsonUtil} 下划线策略：{@code outRequestNo} ↔ {@code out_request_no}。
 *
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012713017">提交申请单</a>
 */
public class EcommerceApplymentRequest {

    /** 业务申请编号 */
    private String outRequestNo;
    /** 主体类型 */
    private String organizationType;
    /** 营业执照/登记证书信息（个体户、企业等有执照主体时必填） */
    private BusinessLicenseInfo businessLicenseInfo;
    /** 法人/经营者身份证信息 */
    private IdCardInfo idCardInfo;
    /** 结算账户信息 */
    private AccountInfo accountInfo;
    /** 超级管理员信息 */
    private ContactInfo contactInfo;
    /** 经营场景信息 */
    private SalesSceneInfo salesSceneInfo;
    /** 商户简称 */
    private String merchantShortname;
    /** 进件结果通知 URL */
    private String notifyUrl;

    public String getOutRequestNo() {
        return outRequestNo;
    }

    public void setOutRequestNo(String outRequestNo) {
        this.outRequestNo = outRequestNo;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public BusinessLicenseInfo getBusinessLicenseInfo() {
        return businessLicenseInfo;
    }

    public void setBusinessLicenseInfo(BusinessLicenseInfo businessLicenseInfo) {
        this.businessLicenseInfo = businessLicenseInfo;
    }

    public IdCardInfo getIdCardInfo() {
        return idCardInfo;
    }

    public void setIdCardInfo(IdCardInfo idCardInfo) {
        this.idCardInfo = idCardInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public SalesSceneInfo getSalesSceneInfo() {
        return salesSceneInfo;
    }

    public void setSalesSceneInfo(SalesSceneInfo salesSceneInfo) {
        this.salesSceneInfo = salesSceneInfo;
    }

    public String getMerchantShortname() {
        return merchantShortname;
    }

    public void setMerchantShortname(String merchantShortname) {
        this.merchantShortname = merchantShortname;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    /**
     * 营业执照/登记证书信息。
     */
    public static class BusinessLicenseInfo {

        /** 营业执照扫描件 MediaID */
        private String businessLicenseCopy;
        /** 营业执照注册号/统一社会信用代码 */
        private String businessLicenseNumber;
        /** 商户名称（执照上） */
        private String merchantName;
        /** 经营者/法定代表人姓名 */
        private String legalPerson;

        public String getBusinessLicenseCopy() {
            return businessLicenseCopy;
        }

        public void setBusinessLicenseCopy(String businessLicenseCopy) {
            this.businessLicenseCopy = businessLicenseCopy;
        }

        public String getBusinessLicenseNumber() {
            return businessLicenseNumber;
        }

        public void setBusinessLicenseNumber(String businessLicenseNumber) {
            this.businessLicenseNumber = businessLicenseNumber;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public String getLegalPerson() {
            return legalPerson;
        }

        public void setLegalPerson(String legalPerson) {
            this.legalPerson = legalPerson;
        }
    }

    /**
     * 身份证信息。
     */
    public static class IdCardInfo {

        /** 身份证人像面 MediaID */
        private String idCardCopy;
        /** 身份证国徽面 MediaID */
        private String idCardNational;
        /** 身份证姓名 */
        private String idCardName;
        /** 身份证号码 */
        private String idCardNumber;
        /** 身份证有效期开始 YYYY-MM-DD */
        private String idCardValidTimeBegin;
        /** 身份证有效期结束 YYYY-MM-DD 或「长期」 */
        private String idCardValidTime;

        public String getIdCardCopy() {
            return idCardCopy;
        }

        public void setIdCardCopy(String idCardCopy) {
            this.idCardCopy = idCardCopy;
        }

        public String getIdCardNational() {
            return idCardNational;
        }

        public void setIdCardNational(String idCardNational) {
            this.idCardNational = idCardNational;
        }

        public String getIdCardName() {
            return idCardName;
        }

        public void setIdCardName(String idCardName) {
            this.idCardName = idCardName;
        }

        public String getIdCardNumber() {
            return idCardNumber;
        }

        public void setIdCardNumber(String idCardNumber) {
            this.idCardNumber = idCardNumber;
        }

        public String getIdCardValidTimeBegin() {
            return idCardValidTimeBegin;
        }

        public void setIdCardValidTimeBegin(String idCardValidTimeBegin) {
            this.idCardValidTimeBegin = idCardValidTimeBegin;
        }

        public String getIdCardValidTime() {
            return idCardValidTime;
        }

        public void setIdCardValidTime(String idCardValidTime) {
            this.idCardValidTime = idCardValidTime;
        }
    }

    /**
     * 结算银行账户信息。
     */
    public static class AccountInfo {

        /** 账户类型 74/75 */
        private String bankAccountType;
        /** 开户银行 */
        private String accountBank;
        /** 银行账号 */
        private String accountNumber;
        /** 开户名称 */
        private String accountName;

        public String getBankAccountType() {
            return bankAccountType;
        }

        public void setBankAccountType(String bankAccountType) {
            this.bankAccountType = bankAccountType;
        }

        public String getAccountBank() {
            return accountBank;
        }

        public void setAccountBank(String accountBank) {
            this.accountBank = accountBank;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }
    }

    /**
     * 超级管理员信息（官方 contact_info 无邮箱字段）。
     */
    public static class ContactInfo {

        /** 超管类型 65/66 */
        private String contactType;
        /** 超管姓名 */
        private String contactName;
        /** 超管证件号（选填） */
        private String contactIdCardNumber;
        /** 超管手机 */
        private String mobilePhone;

        public String getContactType() {
            return contactType;
        }

        public void setContactType(String contactType) {
            this.contactType = contactType;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactIdCardNumber() {
            return contactIdCardNumber;
        }

        public void setContactIdCardNumber(String contactIdCardNumber) {
            this.contactIdCardNumber = contactIdCardNumber;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }
    }

    /**
     * 经营场景信息。
     */
    public static class SalesSceneInfo {

        /** 店铺名称 */
        private String storeName;
        /** 店铺链接（与 storeQrCode 二选一） */
        private String storeUrl;
        /** 店铺二维码 MediaID（与 storeUrl 二选一） */
        private String storeQrCode;

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreUrl() {
            return storeUrl;
        }

        public void setStoreUrl(String storeUrl) {
            this.storeUrl = storeUrl;
        }

        public String getStoreQrCode() {
            return storeQrCode;
        }

        public void setStoreQrCode(String storeQrCode) {
            this.storeQrCode = storeQrCode;
        }
    }
}
