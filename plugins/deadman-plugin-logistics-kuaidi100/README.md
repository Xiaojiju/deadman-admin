# deadman-plugin-logistics-kuaidi100

[快递100](https://api.kuaidi100.com/) 物流渠道插件，实现 extension 中各领域 Provider SPI 及对应 Context/Result 模型。

基于官方 Java SDK：[kuaidi100-api/java-demo](https://github.com/kuaidi100-api/java-demo)（Maven：`com.github.kuaidi100-api:sdk:1.1.3`）。

---

> **⚠️ 测试状态：尚未完成充分测试**
>
> 本插件已实现轨迹、识别、面单、寄件四个领域的 Provider 与 API 网关，但**尚未完成与快递100 生产环境的全面联调**。
> 当前自动化测试仅包含 Mock 轨迹查单的单元测试；订阅回调、面单、寄件等接口请在启用真实账号前自行验证。
> 生产使用前请关闭 `mock-enabled` 并完成全链路测试。

---

## 快速开始

### 1. 引入依赖

与 extension 一并引入（见 [extension README](../../extensions/deadman-extension-logistics/README.md)）。

### 2. 配置

**开发 / 测试（Mock，无需账号）：**

```yaml
deadman:
  plugin:
    logistics-kuaidi100:
      enabled: true
      mock-enabled: true
```

**生产（真实查单）：**

```yaml
deadman:
  plugin:
    logistics-kuaidi100:
      enabled: true
      mock-enabled: false
      key: ${KUAIDI100_KEY}
      customer: ${KUAIDI100_CUSTOMER}
      secret: ${KUAIDI100_SECRET}                    # 面单/寄件必填
      subscribe-salt: ${KUAIDI100_SUBSCRIBE_SALT:}     # 订阅推送验签
      subscribe-callback-url: https://your-domain/client/api/logistics/kuaidi100/subscribe/notify
```

> `key` 与 `customer` 可在 [快递100企业版](https://poll.kuaidi100.com/manager/page/myinfo/enterprise) 获取。实时查单签名规则：`MD5(param + key + customer)` 转大写。

### 3. 调用

```java
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarriers;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;

// 使用平台统一编码 YTO，Service 层自动映射为快递100 的 yuantong
logisticsService.queryTrack(new LogisticsTrackQueryContext(LogisticsCarriers.YTO, "YT1234567890", null), "kuaidi100");

// 顺丰需传手机号后四位
logisticsService.queryTrack(new LogisticsTrackQueryContext(LogisticsCarriers.SF, "SF1234567890", "1234"), "kuaidi100");
```

平台统一编码定义见 extension 的 `LogisticsCarriers`；本插件通过 `Kuaidi100CarrierCodeContributor` 映射为快递100 `com` 字段。

---

## 模块结构

```
kuaidi100/
├── carrier/
│   └── Kuaidi100CarrierCodeContributor   # 统一编码 ↔ 快递100 com 映射
├── provider/                              # 各领域 Provider（对应 extension spi.<domain>）
│   ├── Kuaidi100LogisticsTrackProvider    # spi.track
│   ├── Kuaidi100LogisticsCarrierProvider  # spi.carrier
│   ├── Kuaidi100LogisticsWaybillProvider  # spi.waybill
│   └── Kuaidi100LogisticsShipProvider     # spi.ship
├── client/
│   ├── Kuaidi100LogisticsApiGateway       # 网关接口
│   ├── Kuaidi100LogisticsApiGatewayImpl   # 真实 API 实现
│   └── MockKuaidi100LogisticsApiGateway   # Mock 实现
├── controller/
│   └── Kuaidi100SubscribeNotifyController # 订阅推送回调（匿名 POST）
├── config/
│   └── Kuaidi100LogisticsSecurityConfiguration  # 放行回调路径
└── util/
    └── Kuaidi100TrackMapper               # 轨迹节点映射
```

各领域 Provider 引用 extension 中同包下的 SPI 接口与 record，例如：

| Provider | extension 包 | 主要模型 |
|----------|-------------|----------|
| Track | `spi.track` | `LogisticsTrackQueryContext/Result`、`LogisticsSubscribeContext/Result` |
| Carrier | `spi.carrier` | `LogisticsCarrierDetectResult` |
| Waybill | `spi.waybill` | `LogisticsWaybillOrderContext/Result`、`LogisticsWaybillCancelContext/Result` |
| Ship | `spi.ship` | `LogisticsMerchantShip*`、`LogisticsConsumerShip*` |
| 共享 | `spi.common` | `LogisticsContactInfo` |

---

## Mock 行为

| 单号特征 | 返回状态 |
|----------|----------|
| 包含 `SIGNED`（不区分大小写） | 已签收（state=3） |
| 其他 | 在途（state=0），含 2 条模拟轨迹 |

Mock 模式下识别、面单、寄件等接口返回固定模拟数据，不代表真实 API 行为。

---

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `deadman.plugin.logistics-kuaidi100.enabled` | `false` | 是否启用本插件 |
| `deadman.plugin.logistics-kuaidi100.mock-enabled` | `true` | 是否使用 Mock 网关 |
| `deadman.plugin.logistics-kuaidi100.key` | — | 快递100 授权 key |
| `deadman.plugin.logistics-kuaidi100.customer` | — | 快递100 授权 customer |
| `deadman.plugin.logistics-kuaidi100.secret` | — | 面单/寄件接口密钥 |
| `deadman.plugin.logistics-kuaidi100.subscribe-salt` | — | 订阅推送验签 salt |
| `deadman.plugin.logistics-kuaidi100.subscribe-callback-url` | — | 订阅回调公网 URL |

环境变量：`DEADMAN_PLUGIN_LOGISTICS_KUAIDI100_ENABLED`、`DEADMAN_PLUGIN_LOGISTICS_KUAIDI100_MOCK_ENABLED`、`KUAIDI100_KEY`、`KUAIDI100_CUSTOMER`、`KUAIDI100_SECRET`。

---

## 实现说明

- 查单：调用 SDK `QueryTrack#queryTrack`，开启 `resultv2=4` 获取高级状态与行政区域。
- 订阅：注册推送回调至 `subscribe-callback-url`，由 `Kuaidi100SubscribeNotifyController` 接收并转发至 `LogisticsTrackSubscribePushHandler` SPI。
- SDK 依赖较旧（HttpClient 4.x），隔离在本 plugin 模块，不影响核心栈。
- 查单为无状态实时查询，extension 层不持久化轨迹；业务侧可按需缓存。

---

## 已知测试覆盖

| 场景 | 状态 |
|------|------|
| Mock 轨迹查单（`Kuaidi100LogisticsTrackProviderTest`） | 已覆盖 |
| Mock 网关识别/面单/寄件 | 未单测 |
| 真实快递100 API 联调 | 未验证 |
| 订阅推送回调验签与转发 | 未充分验证 |

---

## 后续可扩展

- 按业务需要补充各领域的集成测试与真实 API 联调用例。
- 其他快递100 能力（如地图轨迹、时效预估）可按同样模式扩展 gateway 与 Provider 方法。
