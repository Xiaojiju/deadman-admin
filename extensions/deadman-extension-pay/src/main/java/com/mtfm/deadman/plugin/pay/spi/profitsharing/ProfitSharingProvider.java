package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 平台收付通分账 Provider SPI。
 */
public interface ProfitSharingProvider {

    /**
     * Provider 标识。
     *
     * @return 标识
     */
    String providerId();

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
     * 请求分账。
     *
     * @param request 请求
     * @return 结果
     */
    ProfitSharingCreateResult create(ProfitSharingCreateRequest request);

    /**
     * 查询分账结果。
     *
     * @param subMchid      二级商户号
     * @param transactionId 微信交易号
     * @param outOrderNo    平台分账单号
     * @return 查询结果
     */
    ProfitSharingQueryResult query(String subMchid, String transactionId, String outOrderNo);

    /**
     * 完结分账。
     *
     * @param request 请求
     * @return 查询形态结果
     */
    ProfitSharingQueryResult finish(ProfitSharingFinishRequest request);

    /**
     * 分账回退。
     *
     * @param request 请求
     * @return 回退结果
     */
    ProfitSharingReturnResult returnOrder(ProfitSharingReturnRequest request);

    /**
     * 添加分账接收方（服务商自身，二级商户维度一次即可）。
     *
     * @param appId        应用 AppId
     * @param type         接收方类型（MERCHANT_ID）
     * @param account      接收方商户号
     * @param relationType 关系类型（如 SERVICE_PROVIDER）
     */
    void addReceiver(String appId, String type, String account, String relationType);
}
