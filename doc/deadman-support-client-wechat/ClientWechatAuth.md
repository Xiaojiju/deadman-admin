# 用户端微信小程序 OAuth 登录与绑定

本文档说明 `deadman-support-client-wechat` 模块的能力、装配方式、接口约定与安全策略。

**前端对接专项文档**：[ClientWechatAuth-Frontend.md](./ClientWechatAuth-Frontend.md)

**相关文档**：

- 管理端同类能力：[AdminWechatAuth.yaml](../../support/deadman-support-wechat/openapi/AdminWechatAuth.yaml)（管理端 OpenAPI）
- JWT 双令牌机制：[JwtTokenRefresh.md](../deadman-security/JwtTokenRefresh.md)
- 手机号绑定 OpenAPI：[WechatMiniprogramController.yaml](../deadman-plugin-wechat/WechatMiniprogramController.yaml)

---

## 1. 模块定位

`deadman-support-client-wechat` 是 **用户端（CLIENT realm）** 与 **微信小程序插件** 之间的桥接层，职责包括：

| 能力 | 说明 |
|------|------|
| 微信登录 | 覆盖插件默认 client 登录实现，未绑定 openid 时不自动开通用户 |
| 账号绑定 | 已有用户：用户名密码 + `bindToken` 完成 openid 关联 |
| 绑定注册 | 新用户：携带 `bindToken` 注册并绑定，成功后直接签发 JWT |
| 手机号绑定 | 已登录用户通过 `getPhoneNumber` code 绑定手机号 |

与管理端 `deadman-support-wechat` 的设计一致：插件只负责微信 API 与通用 LoginProvider 框架，**具体用户体系行为由 support 模块实现**。

---

## 2. 依赖与装配

### 2.1 Maven 依赖

在可运行模块（如 `deadman-app`）中同时引入：

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-component-client</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-wechat</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-client-wechat</artifactId>
</dependency>
```

`deadman-component-client` **不再** 直接依赖微信插件；微信能力通过 support 模块按需组装。

### 2.2 配置项

```yaml
deadman:
  plugin:
    wechat-miniprogram:
      enabled: true
      app-id: ${DEADMAN_WECHAT_MINIPROGRAM_APP_ID}
      app-secret: ${DEADMAN_WECHAT_MINIPROGRAM_APP_SECRET}
      login-bindings:
        - group-id: client   # 必须包含 client，否则跳过桥接 Provider 注册
        # - group-id: admin  # 管理端需 deadman-support-wechat
  support:
    client-wechat:
      enabled: true                              # 默认 true
      bind-token-ttl: 10m                        # bindToken Redis TTL，默认 10 分钟
  component:
    client:
      enabled: true
      jwt:
        secret: ...                              # CLIENT realm 独立密钥
        refresh-cookie-secure: false
```

| 配置 | 环境变量 | 说明 |
|------|----------|------|
| `deadman.support.client-wechat.enabled` | `DEADMAN_SUPPORT_CLIENT_WECHAT_ENABLED` | 桥接总开关 |
| `deadman.support.client-wechat.bind-token-ttl` | `DEADMAN_SUPPORT_CLIENT_WECHAT_BIND_TOKEN_TTL` | 待绑定临时令牌 TTL |
| `deadman.plugin.wechat-miniprogram.login-bindings[].group-id` | — | 须含 `client` |

### 2.3 自动装配条件

`DeadmanClientWechatSupportAutoConfiguration` 在以下条件同时满足时生效：

1. classpath 存在 `WechatApiClient`（已引入 `deadman-plugin-wechat`）
2. classpath 存在 `deadman-component-client`
3. `deadman.support.client-wechat.enabled=true`（默认 true）

---

## 3. 核心组件

```
deadman-support-client-wechat/
├── auth/
│   ├── ClientWechatMiniprogramLoginProvider   # 微信 code 登录（覆盖插件默认）
│   ├── ClientWechatBindLoginProvider          # 已有账号绑定
│   ├── ClientWechatRegisterLoginProvider      # 新用户绑定注册
│   └── ClientWechat*AuthenticationToken     # 各 Provider 认证令牌
├── service/
│   ├── ClientWechatAuthService                # 登录 / 绑定 / 绑定注册业务
│   ├── ClientWechatBindTokenStore             # bindToken Redis 存取
│   └── ClientWechatPendingSession             # code2session 待绑定会话
├── handler/
│   ├── ClientWechatPendingBindLoginHandler    # 待绑定成功 JSON 响应
│   └── ClientWechatPhoneBindingHandler        # 手机号绑定 SPI 实现
├── controller/
│   └── ClientWechatMiniprogramController      # 手机号绑定 HTTP 接口
└── autoconfigure/
    └── ClientWechatLoginProviderRegistrar     # 覆盖插件 client 登录 Bean
```

| 类 | 职责 |
|----|------|
| `ClientWechatLoginProviderRegistrar` | 启动时移除 `wechatMiniprogramLoginProvider_client` 插件默认 Bean，替换为桥接实现 |
| `ClientWechatAuthService.loginByWechatCode` | `code2session` → 已绑定则认证，未绑定则写 Redis 返回 `bindToken` |
| `ClientWechatAuthService.bindAndAuthenticate` | 消费 `bindToken` + 校验密码 + `bindOAuth` |
| `ClientWechatAuthService.registerAndBind` | 消费 `bindToken` + 注册 + `bindOAuth` + 返回已认证用户 |
| `ClientWechatPendingBindLoginHandler` | 实现 `PendingOAuthBindLoginHandler`，待绑定时返回 `ClientWechatPendingBindVO` 而非 JWT |

Redis key 前缀：`deadman:wechat:client:bind:{bindToken}`（见 `RedisKeyConstants.CLIENT_WECHAT_BIND`）。

OAuth 写入 `client_user_account`：`account_type=OAUTH`，`oauth_provider=wechat-miniprogram`，`account_identifier=openid`。

---

## 4. 接口一览

所有登录类接口由 `ProviderLoginFilter` 处理，**HTTP 200 + 业务码** 与现有登录约定一致。成功签发 JWT 时响应体为 `AuthTokenVO`，并写入 Refresh HttpOnly Cookie。

| 操作 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 微信登录 | POST | `/client/api/auth/login/wechat-miniprogram` | 公开 |
| 绑定已有账号 | POST | `/client/api/auth/wechat-miniprogram/bind` | 公开 |
| 绑定注册新账号 | POST | `/client/api/auth/wechat-miniprogram/register` | 公开 |
| 绑定手机号 | POST | `/client/api/wechat-miniprogram/phone/bind` | CLIENT Access Token |
| 普通注册 | POST | `/client/api/auth/register` | 公开（**不含** bindToken，不绑定微信） |

Refresh Cookie（绑定/注册成功与普通登录相同）：

| 属性 | 值 |
|------|-----|
| 名称 | `deadman_client_refresh_token` |
| Path | `/client/api/auth` |
| HttpOnly | `true` |

---

## 5. 业务流程

### 5.1 已绑定 openid — 直接登录

```
小程序 wx.login → code
    → POST /client/api/auth/login/wechat-miniprogram { code }
    → openid 已在 client_user_account
    → 返回 AuthTokenVO + Set-Cookie(refresh)
```

### 5.2 未绑定 — 绑定已有账号

```
wx.login → code
    → POST .../login/wechat-miniprogram
    → 返回 { bindToken, expiresIn, needBind: true }
    → 用户输入已有用户名密码
    → POST .../wechat-miniprogram/bind { bindToken, username, password }
    → 校验密码 + bindOAuth + 返回 AuthTokenVO
```

### 5.3 未绑定 — 注册新账号并绑定

```
wx.login → code
    → POST .../login/wechat-miniprogram
    → 返回 { bindToken, expiresIn, needBind: true }
    → 用户填写注册信息（须携带同一 bindToken）
    → POST .../wechat-miniprogram/register { bindToken, username, password, nickname? }
    → 创建用户 + bindOAuth + 返回 AuthTokenVO
```

> **注意**：`/client/api/auth/register` 普通注册接口**不支持** `bindToken`，不会完成微信绑定。微信场景请使用 `.../wechat-miniprogram/register`。

### 5.4 已登录 — 绑定微信手机号

```
用户已持有 CLIENT Access Token
    → 小程序 getPhoneNumber 获 code
    → POST /client/api/wechat-miniprogram/phone/bind { code }
    → 解密手机号写入 client_user_account(PHONE)
```

---

## 6. 响应结构

### 6.1 已绑定 / 绑定成功 / 绑定注册成功 — `AuthTokenVO`

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "refreshExpiresIn": 604800,
    "userCode": "CL001",
    "nickname": "昵称"
  }
}
```

### 6.2 未绑定 — `ClientWechatPendingBindVO`

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "bindToken": "a1b2c3d4e5f6789012345678901234ab",
    "expiresIn": 600,
    "needBind": true
  }
}
```

前端通过 `needBind === true` 或 `data.bindToken` 是否存在判断进入绑定/注册流程。

---

## 7. 安全策略

| 规则 | 说明 |
|------|------|
| bindToken 一次性 | `consume` 使用 `GETDEL`，用过即失效 |
| bindToken TTL | 默认 10 分钟，过期需重新 `wx.login` |
| 不自动开通用户 | 未绑定 openid 绝不创建用户或签发 JWT |
| openid 独占 | 同一 openid 只能绑定一个用户（`OAUTH_ALREADY_BOUND`） |
| 会话隔离 | CLIENT JWT / Cookie 与管理端完全独立 |

### 常见业务错误码

| code | 含义 |
|------|------|
| `10003` | 用户名已存在（绑定注册时） |
| `12031` | 该微信已被其他用户绑定 |
| `12032` | bindToken 无效或已过期 |
| `40100` | 认证失败（密码错误等，HTTP 仍为 200） |

---

## 8. 与管理端差异

| 项目 | 管理端 `deadman-support-wechat` | 用户端 `deadman-support-client-wechat` |
|------|--------------------------------|----------------------------------------|
| API 前缀 | `/api/auth` | `/client/api/auth` |
| 绑定注册 | 无（须先有管理端账号） | 支持 `.../wechat-miniprogram/register` |
| 自动开通 | 否 | 否（须 bind 或 register） |
| bindToken Redis | `deadman:wechat:admin:bind:` | `deadman:wechat:client:bind:` |
| 配置前缀 | `deadman.support.wechat` | `deadman.support.client-wechat` |

---

## 9. 源码索引

| 路径 | 说明 |
|------|------|
| `support/deadman-support-client-wechat/` | 桥接模块源码 |
| `components/deadman-component-client/.../ClientLoginSuccessHandler` | 登录成功签发 JWT；优先处理 `PendingOAuthBindLoginHandler` |
| `components/deadman-component-client/.../ClientAuthCredentialsService.registerUser` | 普通注册与绑定注册共用 |
| `plugins/deadman-plugin-wechat/` | 微信 API 与通用 LoginProvider 注册 |
| `support/deadman-support-wechat/` | 管理端同类桥接（对照参考） |
