# deadman-plugin-pay-alipay

支付宝渠道插件**骨架**。业务入口不变，仍使用 pay 模块三分域门面：

| 门面 | 支付宝侧职责 |
|------|----------------|
| `DirectPayFacade` | 平台自营收款（直连） |
| `EcommerceTradeFacade` + `SubMerchantFacade` | 二级商户 / 分账（若产品形态需要） |
| `PayoutFacade` | 打款到支付宝账户（**仅 OPERATION 账本**） |

## 与微信的差异（必读）

微信有原生「基本账户 / 运营账户」物理隔离；**支付宝没有等价能力**。  
因此本插件接入时必须：

1. 所有收付仍经对应门面（禁止绕过 `fund_lane`）
2. 分账入账、对公提现记 `plugin_pay_basic_account_flow`
3. 营销/佣金打款记 `plugin_pay_operate_account_flow`
4. 合规中转仍为：`BASIC → 对公 → OPERATION → 打款`

## 启用方式

```yaml
deadman:
  plugin:
    pay-alipay:
      enabled: false          # 骨架默认关闭
      app-id: your-app-id
      providers:
        alipay-jsapi:
          enabled: false      # Stub 打开后仍会拒绝真实下单，直至实现 SDK
```

在 `deadman-app` 引入依赖（已在父 POM `dependencyManagement` 注册）：

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-pay-alipay</artifactId>
</dependency>
```

## 待实现清单

1. 引入支付宝官方 SDK，实现 `AlipayJsapiPaymentProvider`（替换 Stub）
2. `RefundProvider`（`providerId=alipay-jsapi`）
3. `TransferProvider` / 单笔转账 → 仅挂 `PayoutFacade`
4. 如需二级商户：`SubMerchantProvider`（`alipay-sub-merchant`）
