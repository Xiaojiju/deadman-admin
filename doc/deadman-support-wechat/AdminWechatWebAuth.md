# 管理端微信网页扫码 OAuth 登录与绑定

本文档说明 `deadman-support-wechat` 模块对 **网页扫码登录**（`wechat-web`）的桥接能力、装配方式与接口约定。

**前端对接专项文档**：[AdminWechatWebAuth-Frontend.md](./AdminWechatWebAuth-Frontend.md)

**相关文档**：

- 插件总览：[WechatWebLogin.md](../deadman-plugin-wechat/WechatWebLogin.md)
- 用户端网页扫码（对比）：[ClientWechatWebAuth.md](../deadman-support-client-wechat/ClientWechatWebAuth.md)
- 管理端小程序 OAuth：[AdminWechatAuth.yaml](../../support/deadman-support-wechat/openapi/AdminWechatAuth.yaml)
- JWT 双令牌机制：[JwtTokenRefresh.md](../deadman-security/JwtTokenRefresh.md)
- 统一登录 OpenAPI：[WechatLogin.yaml](../deadman-plugin-wechat/WechatLogin.yaml)

---

## 1. 模块定位

`deadman-support-wechat` 在网页扫码场景下的职责：

| 能力 | 说明 |
|------|------|
| 微信登录 | 覆盖插件默认 admin 实现，未绑定 openid 时返回 `bindToken` |
| 账号绑定 | 已有管理端用户：用户名密码 + `bindToken` 完成 openid 关联 |

与用户端不同，管理端 **不支持** 微信扫码绑定注册新账号；须先有管理端账号（或由管理员创建）后再绑定。

身份解析统一走插件门面 `WechatLoginService`，桥接层只处理 **user_base / user_account 与 OAuth 绑定**。

---

## 2. 依赖与装配

### 2.1 Maven 依赖

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-system</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-security</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-wechat</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-wechat</artifactId>
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
        - group-id: admin    # 必须包含 admin
  support:
    wechat:
      enabled: true
      bind-token-ttl: 10m
```

| 配置 | 环境变量 | 说明 |
|------|----------|------|
| `deadman.plugin.wechat-web.enabled` | `DEADMAN_PLUGIN_WECHAT_WEB_ENABLED` | 网页登录插件开关 |
| `deadman.plugin.wechat-web.login-bindings[].group-id` | — | 须含 `admin` |
| `deadman.support.wechat.bind-token-ttl` | `DEADMAN_SUPPORT_WECHAT_BIND_TOKEN_TTL` | bindToken Redis TTL |

### 2.3 自动装配条件

`AdminWechatWebLoginProviderRegistrar` 在以下条件满足时注册：

1. `deadman.plugin.wechat-web.enabled=true`
2. `login-bindings` 包含 `admin`
3. `deadman.support.wechat.enabled=true`（默认 true）

---

## 3. 核心组件

```
deadman-support-wechat/（网页扫码相关）
├── auth/
│   ├── AdminWechatWebLoginProvider       # 覆盖 wechatWebLoginProvider_admin
│   └── AdminWechatWebBindLoginProvider   # 绑定已有管理端账号
├── service/
│   ├── AdminWechatWebAuthService         # 登录 / 绑定业务
│   ├── AdminWechatWebBindTokenStore      # bindToken Redis（独立 key 前缀）
│   └── AdminWechatWebPendingSession      # 待绑定会话
├── handler/
│   └── AdminWechatPendingBindLoginHandler  # 待绑定 JSON 响应（与小程序共用）
└── autoconfigure/
    └── AdminWechatWebLoginProviderRegistrar
```

| 类 | 职责 |
|----|------|
| `AdminWechatWebAuthService.loginByWechatWebCode` | `WechatLoginService.resolve` → 已绑定则认证，未绑定写 Redis 返回 `bindToken` |
| `AdminWechatWebAuthService.bindAndAuthenticate` | 消费 bindToken + 校验管理端密码 + `bindOAuth(wechat-web)` |
| `AdminWechatPendingBindLoginHandler` | 待绑定成功时返回 `{ needBind, bindToken, expiresIn }` |

---

## 4. HTTP 接口

| 场景 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 获取扫码 URL | GET | `/api/wechat/login/wechat-web/initiate` | 公开（插件 API） |
| 微信登录 | POST | `/api/auth/login/wechat-web` | 公开 |
| 绑定已有账号 | POST | `/api/auth/wechat-web/bind` | 公开 |

OAuth 写入 `user_account`：`account_type=OAUTH`，`oauth_provider=wechat-web`，`account_identifier=openid`。

> 管理端登录路径为 `POST /api/auth/login/wechat-web`（`LoginProvider` 标准拼接），非 `/api/auth/wechat-web`。

---

## 5. 响应约定

### 5.1 登录成功（已绑定）

返回 `AuthTokenVO`：`accessToken`、`tokenType`、`expiresIn`；Set-Cookie 写入 `deadman_refresh_token`。

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
| 401 | 绑定用户名密码错误 | 提示用户重试 |

---

## 6. 与用户端差异

| 能力 | 管理端 admin | 用户端 client |
|------|--------------|---------------|
| 桥接模块 | `deadman-support-wechat` | `deadman-support-client-wechat` |
| API 前缀 | `/api/**` | `/client/api/**` |
| 登录 | `POST /api/auth/login/wechat-web` | `POST /client/api/auth/login/wechat-web` |
| 绑定 | `POST /api/auth/wechat-web/bind` | `POST /client/api/auth/wechat-web/bind` |
| 绑定注册 | **不支持** | 支持 `.../wechat-web/register` |
| Refresh Cookie | `deadman_refresh_token` | `deadman_client_refresh_token` |
| 用户表 | `user_base` / `user_account` | `client_user_*` |

用户端文档：[ClientWechatWebAuth.md](../deadman-support-client-wechat/ClientWechatWebAuth.md)
