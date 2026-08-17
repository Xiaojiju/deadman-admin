package com.mtfm.deadman.plugin.pay.constant;

/**
 * 平台资金业务场景：与账户类型硬绑定，写错账户直接拒绝。
 */
public enum PlatformFundBizScene {

    /** 收付通分账入账服务商基本账户（服务费/中转佣金） */
    ECOMMERCE_PROFIT_SHARING_IN(PlatformFundAccountType.BASIC, PlatformFundDirection.IN),

    /** 收付通分账回退出账服务商基本账户（退款前回退平台费/员工中转） */
    ECOMMERCE_PROFIT_SHARING_RETURN_OUT(PlatformFundAccountType.BASIC, PlatformFundDirection.OUT),

    /** 基本账户侧交易退款（若发生） */
    BASIC_TRADE_REFUND_OUT(PlatformFundAccountType.BASIC, PlatformFundDirection.OUT),

    /** 基本账户提现至对公银行卡（人工中转第一步） */
    BASIC_WITHDRAW_TO_BANK(PlatformFundAccountType.BASIC, PlatformFundDirection.OUT),

    /** 企业对公充值进入运营账户（营销预算或中转回充） */
    OPERATE_BANK_RECHARGE(PlatformFundAccountType.OPERATION, PlatformFundDirection.IN),

    /** 商家转账到零钱（员工佣金/营销奖励），仅运营账户 */
    PAYOUT_TO_WALLET(PlatformFundAccountType.OPERATION, PlatformFundDirection.OUT);

    private final String accountType;
    private final String direction;

    PlatformFundBizScene(String accountType, String direction) {
        this.accountType = accountType;
        this.direction = direction;
    }

    /**
     * @return 绑定的账户类型
     */
    public String accountType() {
        return accountType;
    }

    /**
     * @return 资金方向
     */
    public String direction() {
        return direction;
    }
}
