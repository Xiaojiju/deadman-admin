package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通二级商户进件命令（对齐微信「提交申请单」实用必填子集）。
 *
 * @param outRequestNo                 业务申请编号
 * @param organizationType             主体类型
 * @param merchantShortname            商户简称
 * @param bankAccountType              账户类型 74/75
 * @param accountBank                  开户银行
 * @param accountName                  开户名称
 * @param accountNumber                银行账号
 * @param contactType                  超管类型 65/66
 * @param contactName                  超管姓名
 * @param mobilePhone                  超管手机
 * @param contactIdCardNumber          超管证件号（可空）
 * @param storeName                    店铺名称
 * @param storeUrl                     店铺链接（与 storeQrCode 二选一）
 * @param storeQrCode                  店铺二维码 MediaID（与 storeUrl 二选一）
 * @param businessLicenseCopy          营业执照 MediaID（有执照主体时）
 * @param businessLicenseNumber        执照注册号
 * @param businessLicenseMerchantName  执照商户名称
 * @param businessLicenseLegalPerson   执照法人/经营者
 * @param idCardCopy                   身份证人像面 MediaID
 * @param idCardNational               身份证国徽面 MediaID
 * @param idCardName                   身份证姓名
 * @param idCardNumber                 身份证号码
 * @param idCardValidTimeBegin         身份证有效期开始
 * @param idCardValidTime              身份证有效期结束
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012713017">提交申请单</a>
 */
public record WechatEcommerceApplymentCommand(
        String outRequestNo,
        String organizationType,
        String merchantShortname,
        String bankAccountType,
        String accountBank,
        String accountName,
        String accountNumber,
        String contactType,
        String contactName,
        String mobilePhone,
        String contactIdCardNumber,
        String storeName,
        String storeUrl,
        String storeQrCode,
        String businessLicenseCopy,
        String businessLicenseNumber,
        String businessLicenseMerchantName,
        String businessLicenseLegalPerson,
        String idCardCopy,
        String idCardNational,
        String idCardName,
        String idCardNumber,
        String idCardValidTimeBegin,
        String idCardValidTime) {
}
