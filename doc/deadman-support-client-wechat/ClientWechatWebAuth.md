# 用户端微信网页扫码 OAuth 登录与绑定

本文档说明 `deadman-support-client-wechat` 模块对 **网页扫码登录**（`wechat-web`）的桥接能力、装配方式与接口约定。

**前端对接专项文档**：[ClientWechatWebAuth-Frontend.md](./ClientWechatWebAuth-Frontend.md)

**相关文档**：

- 插件总览：[WechatWebLogin.md](../deadman-plugin-wechat/WechatWebLogin.md)
- 管理端网页扫码：[AdminWechatWebAuth.md](../deadman-support-wechat/AdminWechatWebAuth.md)
- 微信小程序桥接（对比）：[ClientWechatAuth.md](./ClientWechatAuth.md)

---

## 1. 模块定位

`deadman-support-client-wechat` 在网页扫码场景下的职责：

| 能力 | 说明 |
|------|------|
| 微信登录 | 覆盖插件默认 client 实现，未绑定 openid 时返回 `bindToken` |
| 账号绑定 | 已有用户：用户名密码 + `bindToken` 完成 openid 关联 |
| 绑定注册 | 新用户：携带 `bindToken` 注册并绑定，成功后直接签发 JWT |

身份解析统一走插件门面 `WechatLoginService`，桥接层只处理 **用户表与 OAuth 绑定**。

---

## 2. 依赖与装配

### 2.1 Maven 依赖

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

### 2.2 配置项

```yaml
deadman:
  plugin:
    wechat-web:
      enabled: true
      app-id: ${DEADMAN_WECHAT_WEB_APP_ID}
      app-secret: ${DEADMAN_WECHAT_WEB_APP_SECRET}
      redirect-uri: ${DEADMAN_WECHAT_WEB_REDIRECT_URI}
      login-bindings:
        - group-id: client    # 必须包含 client
  support:
    client-wechat:
      enabled: true
      bind-token-ttl: 10m
```

| 配置 | 环境变量 | 说明 |
|------|----------|------|
| `deadman.plugin.wechat-web.enabled` | `DEADMAN_PLUGIN_WECHAT_WEB_ENABLED` | 网页登录插件开关 |
| `deadman.plugin.wechat-web.login-bindings[].group-id` | — | 须含 `client` |
| `deadman.support.client-wechat.bind-token-ttl` | `DEADMAN_SUPPORT_CLIENT_WECHAT_BIND_TOKEN_TTL` | bindToken Redis TTL |

### 2.3 自动装配条件

`ClientWechatWebLoginProviderRegistrar` 在以下条件满足时注册：

1. `deadman.plugin.wechat-web.enabled=true`
2. `login-bindings` 包含 `client`
3. `deadman.support.client-wechat.enabled=true`（默认 true）

---

## 3. 核心组件

```
deadman-support-client-wechat/（网页扫码相关）
├── auth/
│   ├── ClientWechatWebLoginProvider          # 覆盖 wechatWebLoginProvider_client
│   ├── ClientWechatWebBindLoginProvider      # 绑定已有账号
│   └── ClientWechatWebRegisterLoginProvider  # 绑定注册新账号
├── service/
│   ├── ClientWechatWebAuthService            # 登录 / 绑定 / 注册业务
│   ├── ClientWechatWebBindTokenStore         # bindToken Redis（独立 key 前缀）
│   └── ClientWechatWebPendingSession         # 待绑定会话（openid/昵称/头像）
└── autoconfigure/
    └── ClientWechatWebLoginProviderRegistrar
```

| 类 | 职责 |
|----|------|
| `ClientWechatWebAuthService.loginByWechatWebCode` | `WechatLoginService.resolve` → 已绑定则认证，未绑定写 Redis 返回 `bindToken` |
| `ClientWechatWebAuthService.bindAndAuthenticate` | 消费 bindToken + 校验密码 + `bindOAuth(wechat-web)` |
| `ClientWechatWebAuthService.registerAndBind` | 消费 bindToken + 注册 + 绑定 + 返回已认证用户 |
| `ClientWechatPendingBindLoginHandler` | 待绑定成功时返回 `{ needBind, bindToken, expiresIn }`（与小程序共用） |

---

## 4. HTTP 接口

| 场景 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 获取扫码 URL | GET | `/api/wechat/login/wechat-web/initiate` | 公开 |
| 微信登录 | POST | `/client/api/auth/login/wechat-web` | 公开 |
| 绑定已有账号 | POST | `/client/api/auth/wechat-web/bind` | 公开 |
| 绑定注册 | POST | `/client/api/auth/wechat-web/register` | 公开 |

OAuth 写入 `client_user_account`：`account_type=OAUTH`，`oauth_provider=wechat-web`，`account_identifier=openid`。

---

## 5. 响应约定

### 5.1 登录成功（已绑定）

返回 `AuthTokenVO`：`accessToken`、`tokenType`、`expiresIn`；同时 Set-Cookie 写入 Refresh Token。

### 5.2 待绑定

```json
{
  "code": 0,
  "data": {
    "bindToken": "...",
    "expiresIn": 600,
    "needBind": true
  }
}
```

### 5.3 常见错误码

| code | 含义 | 处理 |
|------|------|------|
| 12032 | bindToken 无效或过期 | 重新扫码授权 |
| 400 | state 无效、code 为空等 | 检查参数或重新 initiate |

---

## 6. 与管理端差异

管理端网页扫码见 [AdminWechatWebAuth.md](../deadman-support-wechat/AdminWechatWebAuth.md)：无绑定注册，API 前缀为 `/api/**`。
