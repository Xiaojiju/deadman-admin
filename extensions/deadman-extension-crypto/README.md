# deadman-extension-crypto 使用文档

加解密**能力延伸**模块：对外统一 `CryptoFacade` 门面，内部按策略选择算法。

默认实现：**AES-256-GCM 对称信封加密**，支持 **默认密钥 + 模块密钥** 双轨道，以及 **附加密钥** 做轮换解密。可在 Spring Boot 中自动装配，也可通过工厂**脱离容器**使用。

---

## 目录

- [快速开始](#快速开始)
- [设计概览](#设计概览)
- [配置说明](#配置说明)
- [使用方法](#使用方法)
- [AAD 用途绑定](#aad-用途绑定)
- [密钥轮换](#密钥轮换)
- [密文格式](#密文格式)
- [行为约定与错误码](#行为约定与错误码)
- [扩展新算法](#扩展新算法)
- [包结构](#包结构)
- [常见问题](#常见问题)

---

## 快速开始

### 1. 引入依赖

在业务模块或 `deadman-app` 的 `pom.xml` 中：

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-extension-crypto</artifactId>
</dependency>
```

### 2. 生成并配置密钥

```bash
# 生成 32 字节密钥（Base64）
openssl rand -base64 32
```

写入环境变量（**禁止**把真实密钥提交进仓库）：

```bash
export DEADMAN_CRYPTO_DEFAULT_KEY='<上一步输出>'
export DEADMAN_CRYPTO_PAY_KEY='<另生成一把，给 pay 模块用>'
```

`application.yaml` 示例：

```yaml
deadman:
  plugin:
    crypto:
      enabled: true
      require-keys: true
      strict-module-key: true
      default-algorithm: AES_GCM_ENVELOPE
      default-key-id: default
      default-key: ${DEADMAN_CRYPTO_DEFAULT_KEY}
      module-keys:
        pay:
          key-id: pay
          key: ${DEADMAN_CRYPTO_PAY_KEY}
```

> 默认 `require-keys=true`：启用加密却未配置任何密钥时，**应用启动失败**（防止误以为已加密、实际无密钥）。

### 3. 调用门面

```java
@Service
@RequiredArgsConstructor
public class DemoService {

    private final CryptoFacade cryptoFacade;

    public String saveSensitive(String plain) {
        CryptoContext ctx = CryptoContext.forModule("pay")
                .withPurpose("demo.field");
        return cryptoFacade.encrypt(plain, ctx);
    }

    public String loadSensitive(String stored) {
        return cryptoFacade.decrypt(
                stored,
                CryptoContext.forModule("pay").withPurpose("demo.field"));
    }
}
```

---

## 设计概览

```
业务模块
    │  只依赖 CryptoFacade
    ▼
CryptoFacade（门面）
    │  按 algorithmId 选策略
    ▼
EncryptionStrategy（策略 SPI）
    │  默认 AesGcmEnvelopeEncryptionStrategy
    ▼
CryptoKeyRegistry（密钥）
    ├── default-key          默认 KEK
    ├── module-keys.<code>   模块加密 KEK
    └── additional-keys      轮换后仅解密的历史 KEK
```

| 场景 | 用法 |
|------|------|
| Spring Boot | 注入 `CryptoFacade`（自动配置） |
| 无 Spring / 单测 / 批处理 | `CryptoFacades.create(properties)` |

### 信封流程

1. 随机生成 DEK（AES-256）
2. 用 DEK + AES-GCM 加密明文（可选 AAD）
3. 用 KEK 包装 DEK
4. 编码为可落库字符串；解密时按密文中的 `keyId` 取对应 KEK

---

## 配置说明

配置前缀：`deadman.plugin.crypto`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled` | `true` | 是否启用；为 `false` 时加解密 **fail-closed** 抛错（不再静默透传明文） |
| `require-keys` | `true` | 启用却无任何密钥时，启动 / 工厂创建失败 |
| `strict-module-key` | `true` | 指定了 `moduleCode` 但未配置该模块密钥时失败，**禁止**静默回退 `default-key` |
| `default-algorithm` | `AES_GCM_ENVELOPE` | 默认算法标识 |
| `default-key-id` | `default` | 默认密钥写入密文的 `keyId` |
| `default-key` | — | 默认 KEK，Base64(32 字节) |
| `module-keys.<module>.key-id` | 模块名 | 该模块密文中的 `keyId` |
| `module-keys.<module>.key` | — | 模块 KEK，Base64(32 字节) |
| `additional-keys.<keyId>` | — | 仅用于解密的历史/轮换密钥 |

完整示例见 `deadman-app` 的 `application-example.yaml`。

### 密钥生成

```bash
openssl rand -base64 32
```

`keyId` / `algorithm` / 模块名仅允许：`[A-Za-z0-9_-]`（不能含点号，避免破坏密文分段）。

---

## 使用方法

### Spring 注入（推荐）

```java
private final CryptoFacade cryptoFacade;

// 1) 默认密钥轨道（无 AAD）
String cipher = cryptoFacade.encrypt("张三");
String plain  = cryptoFacade.decrypt(cipher);

// 2) 按模块加密（无 purpose 时不写 AAD）
String cipher2 = cryptoFacade.encryptForModule("张三", "pay");

// 3) 模块 + 字段用途（推荐敏感字段）
CryptoContext ctx = CryptoContext.forModule("pay")
        .withPurpose("transfer.user_name");
String cipher3 = cryptoFacade.encrypt("张三", ctx);
String plain3  = cryptoFacade.decrypt(cipher3, ctx);

// 4) 显式算法
CryptoContext full = CryptoContext.of(
        "pay",
        CryptoAlgorithms.AES_GCM_ENVELOPE,
        "transfer.user_name");
cryptoFacade.encrypt("张三", full);

// 5) 二进制
byte[] raw = CryptoFacade.utf8("敏感内容");
String token = cryptoFacade.encryptBytes(raw, ctx);
byte[] back = cryptoFacade.decryptBytes(token, ctx);
```

### 脱离 Spring 容器

```java
CryptoPluginProperties props = new CryptoPluginProperties();
props.setEnabled(true);
props.setRequireKeys(true);
props.setStrictModuleKey(true);
props.setDefaultKey(Base64.getEncoder().encodeToString(/* 32 bytes */));

CryptoPluginProperties.ModuleKeyProperties payKey =
        new CryptoPluginProperties.ModuleKeyProperties();
payKey.setKeyId("pay");
payKey.setKey(Base64.getEncoder().encodeToString(/* 另 32 bytes */));
props.setModuleKeys(Map.of("pay", payKey));

CryptoFacade facade = CryptoFacades.create(props);
String cipher = facade.encrypt("hello", CryptoContext.forModule("pay").withPurpose("demo"));
```

自定义策略列表：

```java
CryptoFacades.create(props, List.of(new AesGcmEnvelopeEncryptionStrategy(), /* 其它 */));
```

### 新业务模块接入步骤

1. 在本模块（或业务包）定义模块编码常量，例如 `"order"`
2. 定义字段用途常量，例如 `"order.receiver_phone"`
3. yaml 增加：

```yaml
deadman.plugin.crypto.module-keys.order:
  key-id: order
  key: ${DEADMAN_CRYPTO_ORDER_KEY}
```

4. 加解密始终使用**同一组** `moduleCode` + `purpose`：

```java
CryptoContext ctx = CryptoContext.forModule("order").withPurpose("order.receiver_phone");
String cipher = cryptoFacade.encrypt(phone, ctx);
String plain  = cryptoFacade.decrypt(cipher, ctx);
```

> `CryptoModuleCodes.PAY` 仅为便利常量；业务也可在各自模块自建常量（如支付侧 `PayCryptoPurposes`）。

### 支付模块已接入示例

转账收款人姓名由 `PaySensitiveCryptoHelper` 处理：

- 模块：`pay`
- 用途：`transfer.user_name`（`PayCryptoPurposes.TRANSFER_USER_NAME`）
- 无 `CryptoFacade` 时**拒绝**落库（禁止明文）
- 解密兼容：历史明文；以及整改前无 AAD 的旧密文（自动回退）

业务侧一般无需直接调门面，走转账创建 / 派发即可。

---

## AAD 用途绑定

为防止「同一 KEK 下，A 字段密文被拷到 B 字段仍能解密」，加密时可设置 `purpose`：

| `purpose` | AAD 内容 | 说明 |
|-----------|----------|------|
| 空 | 空数组 | 兼容历史无 AAD 密文 |
| 非空 | UTF-8(`{module}\|{purpose}`) | 解密必须传相同 module + purpose |

```java
// 加密
cryptoFacade.encrypt(name, CryptoContext.forModule("pay").withPurpose("transfer.user_name"));

// 用错 purpose → CRYPTO_DECRYPT_FAILED
cryptoFacade.decrypt(cipher, CryptoContext.forModule("pay").withPurpose("other.field"));
```

**约定**：敏感字段一律带 `purpose`；解密 API 使用与加密相同的 `CryptoContext`。

---

## 密钥轮换

1. 生成新密钥，将模块 `key-id` 改为新 id（如 `pay` → `pay-v2`），`key` 换新材料  
2. 把旧密钥放进 `additional-keys`，**keyId 保持加密时写入密文的旧 id**：

```yaml
deadman:
  plugin:
    crypto:
      module-keys:
        pay:
          key-id: pay-v2
          key: ${DEADMAN_CRYPTO_PAY_V2_KEY}
      additional-keys:
        pay: ${DEADMAN_CRYPTO_PAY_OLD_KEY}   # 旧密文里的 keyId=pay
```

3. 新数据用 `pay-v2` 加密；旧数据按密文 `keyId` 自动命中 `additional-keys` 解密  
4. 确认无旧密文后，可移除对应 `additional-keys` 项

---

## 密文格式

可落库字符串（点分六段，二进制段为 Base64URL 无 padding）：

```
v1.{algorithm}.{keyId}.{wrappedDek}.{iv}.{ciphertext}
```

示例（示意）：

```
v1.AES_GCM_ENVELOPE.pay.xxxx.yyyy.zzzz
```

| 规则 | 说明 |
|------|------|
| 前缀 `v1.` | 本模块密文标记 |
| 非此前缀 | `decrypt` 视为历史明文，原样返回 |
| `algorithm` / `keyId` | 仅 `[A-Za-z0-9_-]` |

---

## 行为约定与错误码

### 行为

| 场景 | 行为 |
|------|------|
| 明文为空 / 空白 | 加密原样返回，不产生密文 |
| `enabled=false` | 加解密抛 `CRYPTO_CONFIG_INVALID`（fail-closed） |
| 无任何密钥且 `require-keys=true` | 启动或 `CryptoFacades.create` 失败 |
| 指定模块无密钥且 `strict-module-key=true` | 加密抛 `CRYPTO_KEY_NOT_FOUND` |
| 历史明文（非 `v1.`） | 解密原样返回 |
| purpose 不一致 | 解密抛 `CRYPTO_DECRYPT_FAILED` |

### 错误码（`ResultCode`）

| 码 | 常量 | 含义 |
|----|------|------|
| 14401 | `CRYPTO_CONFIG_INVALID` | 配置无效 / 未启用 / 密钥格式非法 |
| 14402 | `CRYPTO_KEY_NOT_FOUND` | 密钥不存在或严格模式下模块密钥缺失 |
| 14403 | `CRYPTO_ALGORITHM_UNSUPPORTED` | 不支持的算法 |
| 14404 | `CRYPTO_ENCRYPT_FAILED` | 加密失败 |
| 14405 | `CRYPTO_DECRYPT_FAILED` | 解密失败（含 AAD 不匹配） |
| 14406 | `CRYPTO_PAYLOAD_INVALID` | 密文格式无效 |

---

## 扩展新算法

1. 实现 `EncryptionStrategy`（`algorithmId` / `encrypt` / `decrypt`，支持 `aad` 参数）
2. **Spring**：注册为 Bean（`List<EncryptionStrategy>` 会自动收集）；或在 `DeadmanCryptoAutoConfiguration` 旁增加 `@Bean`
3. **无容器**：`CryptoFacades.create(props, List.of(new YourStrategy(), ...))`
4. 配置 `default-algorithm` 或在 `CryptoContext` 中指定 `algorithmId`
5. 保证密文 `algorithm` 段与 `algorithmId()` 一致，且符合 `CryptoTokenIds` 字符集

默认策略类：`AesGcmEnvelopeEncryptionStrategy`（**无** `@Component`，由自动配置显式注册，便于脱离 Spring）。

---

## 包结构

```
com.mtfm.deadman.plugin.crypto
├── facade          CryptoFacade / DefaultCryptoFacade / CryptoFacades
├── spi             CryptoContext / EncryptionStrategy / EncryptedPayload
├── strategy        AesGcmEnvelopeEncryptionStrategy
├── key             CryptoKeyRegistry
├── util            CryptoPayloadCodec / CryptoAad / CryptoTokenIds
├── config          CryptoPluginProperties
├── constant        CryptoAlgorithms / CryptoModuleCodes
└── autoconfigure   DeadmanCryptoAutoConfiguration
```

业务侧建议只依赖：`facade`、`spi.CryptoContext`、`constant`（按需）。

---

## 常见问题

**Q: 启动报 require-keys / 未配置任何密钥？**  
A: 设置 `DEADMAN_CRYPTO_DEFAULT_KEY` 或至少一个 `module-keys.*.key`。本地临时关闭校验可设 `require-keys: false`（不推荐上生产）。

**Q: 调用 `encryptForModule("pay", ...)` 报未配置模块密钥？**  
A: 默认 `strict-module-key=true`。请配置 `module-keys.pay.key`，或显式关闭严格模式（会回退 default，隔离失效）。

**Q: 解密报 CRYPTO_DECRYPT_FAILED，但密钥看起来对？**  
A: 检查加密/解密的 `purpose`（及 module）是否一致；错字段用途会导致 GCM 失败。

**Q: 库里还有整改前的旧密文？**  
A: 无 `purpose` 的旧密文用 `decrypt(cipher)`（默认空 AAD）可解。支付姓名 Helper 已做「先带 AAD、失败再兼容」回退。

**Q: 能否完全不用 Spring？**  
A: 可以。用 `CryptoFacades.create(properties)`；无需启动 Boot。POM 仍带 spring-boot 依赖，但运行路径不依赖 `ApplicationContext`。

**Q: 密钥能写在 yaml 明文里吗？**  
A: 仅限本地联调。生产必须环境变量 / 密钥管理系统注入，禁止提交真实密钥。

---

## 相关代码入口

| 类型 | 类 |
|------|-----|
| 门面 API | `com.mtfm.deadman.plugin.crypto.facade.CryptoFacade` |
| 工厂 | `com.mtfm.deadman.plugin.crypto.facade.CryptoFacades` |
| 上下文 | `com.mtfm.deadman.plugin.crypto.spi.CryptoContext` |
| 配置 | `com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties` |
| 单测示例 | `.../facade/DefaultCryptoFacadeTest` |
