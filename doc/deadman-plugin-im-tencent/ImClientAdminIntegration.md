# Client / Admin 腾讯云 IM 接入说明

本文说明工程信息平台中 **用户端（client）** 与 **管理端（admin）** 如何接入腾讯云 IM：模块分工、装配配置、接口约定与前端对接流程。

**相关代码：**

| 层级 | 模块 | 职责 |
|------|------|------|
| 插件 | `deadman-plugin-im-tencent` | UserSig 签发、账号同步、`ImUserRealmBridge` SPI、映射表 |
| C 端桥接 | `deadman-support-client-im` | `realmId = client`，暴露 `/client/api/im/**` |
| 管理端桥接 | `deadman-support-admin-im` | `realmId = admin`，暴露 `/api/im/**` |

插件自身 README：[`plugins/deadman-plugin-im-tencent/README.md`](../../plugins/deadman-plugin-im-tencent/README.md)

---

## 1. 架构总览

插件与用户体系解耦：只认识抽象主体 `ImSubject(realmId, subjectId)`。具体「谁在登录、昵称头像从哪读」由 Support 模块实现 `ImUserRealmBridge` 并注册为 Spring Bean。

```
┌─────────────────────┐     ┌─────────────────────┐
│  C 端 App / 小程序   │     │  管理端 / 客服台      │
│  TIM SDK            │     │  TIM SDK            │
└──────────┬──────────┘     └──────────┬──────────┘
           │ GET credential            │ GET credential
           │ CLIENT JWT                │ Admin JWT
           ▼                           ▼
┌──────────────────────┐    ┌──────────────────────┐
│ deadman-support-     │    │ deadman-support-     │
│ client-im            │    │ admin-im             │
│ ClientImUserRealmBridge │ │ AdminImUserRealmBridge │
│ realm = client       │    │ realm = admin        │
└──────────┬───────────┘    └──────────┬───────────┘
           │                           │
           └─────────────┬─────────────┘
                         ▼
           ┌─────────────────────────┐
           │ deadman-plugin-im-tencent │
           │ ImService               │
           │ account_import / UserSig│
           │ plugin_im_user_account  │
           └─────────────────────────┘
```

两套 Support **可同时启用**，在腾讯云 IM 中通过不同 `imUserId`（默认 `{realm}_{subjectId}`）隔离账号空间。

---

## 2. 用户域对照

| 项 | Client | Admin |
|----|--------|-------|
| `realmId` | `client` | `admin` |
| Support 模块 | `deadman-support-client-im` | `deadman-support-admin-im` |
| Bridge | `ClientImUserRealmBridge` | `AdminImUserRealmBridge` |
| 主体主键 `subjectId` | `ClientLoginUser.userCode` | `LoginUser.userCode` |
| 资料来源 | `ClientUserService.getProfileByUserCode` | `UserService.getProfileByUserCode` |
| 资料字段 | nickname、avatar、status | nickname、avatar、status |
| 配置前缀 | `deadman.support.client-im` | `deadman.support.admin-im` |
| API 前缀 | `/client/api/im` | `/api/im` |
| 鉴权 Token | 用户端 JWT（`Authorization: Bearer {clientAccessToken}`） | 管理端 JWT（`Authorization: Bearer {adminAccessToken}`） |

禁用用户（`status != ACTIVE`）签发凭证时返回业务码 `IM_USER_DISABLED`（14303）。

---

## 3. 依赖与装配

### 3.1 Maven（`deadman-app` 已引入）

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-im-tencent</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-client-im</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-admin-im</artifactId>
</dependency>
```

依赖约定：

- 插件仅依赖 `deadman-common`、`deadman-core`，**禁止**直接依赖 `deadman-system` / `deadman-component-client`
- Client Support → `deadman-component-client` + 插件
- Admin Support → `deadman-system` + `deadman-security` + 插件

### 3.2 数据库

执行插件脚本：

- MySQL：`plugins/deadman-plugin-im-tencent/src/main/resources/db/im-tencent/schema.sql`
- H2 测试：`schema-h2.sql`

核心表 `plugin_im_user_account`：`(realm_id, subject_id) → im_user_id`，并缓存昵称/头像同步快照。

### 3.3 配置

参考 `deadman-app/src/main/resources/application-example.yaml`：

```yaml
deadman:
  plugin:
    im-tencent:
      enabled: ${DEADMAN_PLUGIN_IM_TENCENT_ENABLED:true}
      # 本地/联调可开 Mock；生产务必 false，并配置真实 sdk-app-id / secret-key
      mock-enabled: ${DEADMAN_PLUGIN_IM_TENCENT_MOCK_ENABLED:true}
      sdk-app-id: ${TENCENT_IM_SDK_APP_ID:}
      secret-key: ${TENCENT_IM_SECRET_KEY:}
      admin-identifier: ${TENCENT_IM_ADMIN_IDENTIFIER:administrator}
      user-sig-expire-seconds: ${TENCENT_IM_USER_SIG_EXPIRE_SECONDS:86400}
      # 默认生成如 client_CL20260001、admin_ADM001
      user-id-template: ${TENCENT_IM_USER_ID_TEMPLATE:{realm}_{subjectId}}
  support:
    client-im:
      enabled: ${DEADMAN_SUPPORT_CLIENT_IM_ENABLED:true}
    admin-im:
      enabled: ${DEADMAN_SUPPORT_ADMIN_IM_ENABLED:true}
```

| 配置 | 说明 |
|------|------|
| `deadman.plugin.im-tencent.enabled` | 插件总开关（默认 true） |
| `mock-enabled` | `true` 时走 Mock 网关，不调腾讯云；亦在缺少 SDKAppID/SecretKey 时自动 Mock |
| `user-id-template` | 占位符 `{realm}`、`{subjectId}`；结果按腾讯云限制截断至 32 字节 |
| `deadman.support.client-im.enabled` | C 端桥接（默认 true，且 classpath 需有 `ImService`） |
| `deadman.support.admin-im.enabled` | 管理端桥接（默认 true） |

自动配置条件：Support 模块 `@ConditionalOnClass(ImService)` + 对应 `enabled=true`。

---

## 4. 签发与同步流程

客户端或管理端拿到 JWT 后，请求各自的 credential 接口，后端逻辑由 `ImService` 统一编排：

```
1. Bridge.resolveCurrentSubject()     ← 从 SecurityContext 取 userCode
2. Bridge.resolveProfileSource()      ← 读昵称/头像/是否可用
3. ensureAccount()                    ← 查/建 plugin_im_user_account
4. syncIfNeeded()                     ← 昵称头像变更或从未同步则 account_import
5. generateUserSig(imUserId)          ← 返回 sdkAppId + imUserId + userSig + expireAt
```

SecretKey **仅留在后端**；前端只用返回的 UserSig 调用腾讯云 TIM SDK `login`。

---

## 5. API 约定

统一响应包装：`Result<T>`（`code` / `message` / `data`）。

### 5.1 用户端：`ClientImController`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/client/api/im/credential` | 用户端 JWT | 为当前登录 C 端用户签发 IM 凭证 |

**响应 `data`（`ImCredentialVO`）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `sdkAppId` | long | 腾讯云 IM SDKAppID |
| `imUserId` | string | 腾讯云 UserID |
| `userSig` | string | 登录签名 |
| `expireAt` | long | 过期时间（UTC 秒级时间戳） |

示例：

```http
GET /client/api/im/credential
Authorization: Bearer {clientAccessToken}
```

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "sdkAppId": 1400000000,
    "imUserId": "client_CL20260001",
    "userSig": "...",
    "expireAt": 1721145600
  }
}
```

### 5.2 管理端：`AdminImController`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/im/credential` | 管理端 JWT | 为当前登录管理员签发 IM 凭证 |
| GET | `/api/im/users/lookup` | 管理端 JWT | 按域 + 业务主键查 IM UserID |

**Lookup 查询参数：**

| 参数 | 必填 | 说明 |
|------|------|------|
| `realm` | 是 | 用户域，如 `client`、`admin` |
| `subjectId` | 是 | 域内稳定主键（userCode） |

**响应 `data`（`ImUserLookupVO`）：** `realmId`、`subjectId`、`imUserId`

典型客服场景：管理端先 `lookup?realm=client&subjectId=CL20260001` 得到对端 `imUserId`，再用自身 credential 登录 TIM 后发起单聊。

> 映射尚未建立（对端从未成功签发过 credential）时返回 `IM_USER_NOT_FOUND`（14305）。

---

## 6. 前端对接要点

### 6.1 用户端 / 管理端通用步骤

1. 完成各自体系的登录，拿到对应 JWT。
2. 请求 credential 接口（见上表）。
3. 使用返回的 `sdkAppId`、`imUserId`、`userSig` 初始化 TIM SDK 并 `login`。
4. UserSig 临近 `expireAt` 时重新拉取 credential（勿在前端伪造签名）。

### 6.2 管理端客服 / 跨域会话

1. 管理员调用 `GET /api/im/credential` 登录 TIM。
2. 业务侧已知对方 `userCode` 时，调用  
   `GET /api/im/users/lookup?realm=client&subjectId={userCode}`。
3. 使用返回的 `imUserId` 作为对端 UserID 创建会话。

跨域示例：`client_CL20260001` ↔ `admin_ADM001`（具体 ID 取决于 `user-id-template`）。

---

## 7. 错误码

| 码 | 枚举 | 含义 |
|----|------|------|
| 14301 | `IM_CONFIG_INVALID` | 插件配置无效（缺 SDKAppID/SecretKey/管理员标识，或 UserID 映射冲突） |
| 14302 | `IM_REALM_UNKNOWN` | 用户域未注册 Bridge，或 Bridge 拒绝了错误 realm |
| 14303 | `IM_USER_DISABLED` | 用户非 ACTIVE，拒绝签发/同步 |
| 14304 | `IM_ACCOUNT_SYNC_FAILED` | 腾讯云账号同步失败 |
| 14305 | `IM_USER_NOT_FOUND` | lookup 时映射不存在 |
| 401 | `UNAUTHORIZED` | 未登录或无法从 SecurityContext 解析主体 |

---

## 8. 扩展新用户域

若新增第三套用户体系（例如商家独立账号）：

1. 新增 Support 模块，实现 `ImUserRealmBridge`（`realmId()` / `resolveCurrentSubject()` / `resolveProfileSource()`）。
2. 可选：暴露该域 REST（复用 `ImService.issueCredentialForCurrentUser(realmId)`）。
3. 在 app 中引入依赖并配置开关；无需改插件内核。

---

## 9. 本地联调建议

| 场景 | 建议 |
|------|------|
| 无腾讯云密钥 | 保持 `mock-enabled: true`，接口仍返回凭证结构，网关 Mock |
| 生产 / 真机 TIM | `mock-enabled: false`，配置真实 `sdk-app-id`、`secret-key`，并初始化 `plugin_im_user_account` 表 |
| 只测 C 端 | 可关掉 `admin-im.enabled`，反之亦然 |
| 单元测试 | 见插件模块 `ImServiceTest`、`TencentImTlsSigApiV2Test` |
