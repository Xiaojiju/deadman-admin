package com.mtfm.deadman.plugin.pay.spi.merchant;

/**
 * 二级商户（入驻商户）进件 / 绑定 SPI，渠道无关抽象。
 * <p>
 * 微信：电商收付通进件；支付宝：商家进件/二级商户开通。业务只依赖本接口。
 */
public interface SubMerchantProvider {

    /**
     * Provider 标识，如 {@code wechat-ecommerce-sub-merchant}。
     *
     * @return 标识
     */
    String providerId();

    /**
     * 支付平台：WECHAT / ALIPAY。
     *
     * @return 平台
     */
    String payPlatform();

    /**
     * 是否支持指定标识。
     *
     * @param providerId 标识
     * @return 是否支持
     */
    default boolean supports(String providerId) {
        return providerId().equals(providerId);
    }

    /**
     * 提交进件申请。
     *
     * @param command 进件命令
     * @return 进件结果
     */
    SubMerchantApplyResult submitApplyment(SubMerchantApplyCommand command);

    /**
     * 按业务申请单号查询进件状态。
     *
     * @param outRequestNo 业务申请单号
     * @return 查询结果
     */
    SubMerchantApplyResult queryApplyment(String outRequestNo);

    /**
     * 上传进件媒体文件（证件照、营业执照、店铺二维码等），返回渠道 MediaID。
     * <p>
     * 默认不支持；微信 Provider 实现 {@code /v3/merchant/media/upload}。
     *
     * @param fileName 文件名（含合法图片后缀）
     * @param content  文件二进制
     * @return 上传结果
     */
    default SubMerchantMediaUploadResult uploadMedia(String fileName, byte[] content) {
        throw new UnsupportedOperationException("当前二级商户 Provider 不支持媒体上传：" + providerId());
    }
}
