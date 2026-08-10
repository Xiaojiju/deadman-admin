# deadman-plugin-pay-mock

Mock 支付 **Provider 实现**（`providerId = mock`），依赖 [deadman-extension-pay](../../extensions/deadman-extension-pay/README.md)。

用于本地 / 测试环境模拟完整支付链路，无需真实商户号或微信配置。

## 能力

| 能力 | 说明 |
|------|------|
| Provider | `MockPaymentProvider`（`payPlatform=MOCK`，`payMethod=MOCK`） |
| 预下单 | 返回假 `prepayId` 与 `PaymentClientInvokeParams` |
| 自动完成 | 默认预下单后立即查单成功并发布支付事件（无需回调） |
| 查单 | 默认返回 `SUCCESS` |
| 回调 | `POST /client/api/pay/mock/notify`（可选，自动完成关闭时使用） |

## 配置

```yaml
deadman:
  plugin:
    pay:
      # 本地联调可切为 mock
      default-provider: mock
      enabled: true
    pay-mock:
      enabled: true
      auto-complete-on-prepay: true
      notify-endpoint: /client/api/pay/mock/notify
```

| 配置项 | 默认 | 说明 |
|--------|------|------|
| `deadman.plugin.pay-mock.enabled` | `false` | 是否启用 Mock Provider |
| `deadman.plugin.pay-mock.auto-complete-on-prepay` | `true` | 预下单后自动完成支付并发布事件 |
| `deadman.plugin.pay-mock.notify-endpoint` | `/client/api/pay/mock/notify` | 回调路径 |

## 联调方式

**方式一：自动完成（推荐，默认开启）**

业务侧 `paymentMethod=mock`（或 `default-provider: mock`）预下单后，PayService 会立即将支付单置为 `SUCCESS` 并发布事件，会员订单会同步激活，无需回调。

**方式二：回调**（将 `auto-complete-on-prepay` 设为 `false` 后使用）

```bash
curl -X POST http://localhost:8080/client/api/pay/mock/notify \
  -H "Content-Type: application/json" \
  -d '{"out_trade_no":"PO...","transaction_id":"mock_tx_001","status":"SUCCESS"}'
```

**方式三：主动查单**

预下单后调用 `PayService.syncOrderFromChannel(outTradeNo)`，或等待支付插件定时同步任务；`queryOrder` 会返回 `SUCCESS`。

业务侧传 `paymentMethod=mock`，或将 `deadman.plugin.pay.default-provider` 设为 `mock`。

> 生产环境请保持 `enabled: false`。
