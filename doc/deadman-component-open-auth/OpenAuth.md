# 开放授权 — 第三方应用对接指南

本文档面向第三方后端（如 Python Agent）与小程序/H5 前端，说明如何对接开放授权（类微信 code 换 token）流程。

后端 API 定义见：

- [OpenAuthController.yaml](OpenAuthController.yaml) — 管理端 App 管理、Token 兑换
- [ClientOpenAuthController.yaml](../deadman-support-client-open-auth/ClientOpenAuthController.yaml) — 用户端申请 auth_code

---

## 1. 对接前必读

| 角色 | 职责 |
|------|------|
| 管理端 | 创建开放应用，获得 `appId` + `clientSecret` |
| 前端 | 用户登录后申请 `auth_code`，传给第三方 |
| 第三方后端 | 用 `client_id` + `client_secret` + `code` 换 `open_access_token` |

**密钥安全**：`clientSecret` 仅保存在第三方服务端，禁止写入前端或小程序代码。

---

## 2. 对接流程

```
用户 → 登录业务系统（client JWT）
     → POST /client/api/open/codes { appId }
     → 获得 code（5min，一次性）
     → 将 code 交给 Python Agent

Python Agent → POST /open-api/oauth/token
              { grant_type, client_id, client_secret, code }
              → 获得 open_access_token + subject + scope
              → 按 scope 过滤知识库检索
```

---

## 3. 管理端：创建应用

`POST /api/open-apps`（管理端 JWT + `open-app:create`）

```json
{
  "appName": "Python AI Agent",
  "description": "知识库 Agent",
  "allowedRealms": ["client"],
  "defaultScopes": ["kb:search", "kb:doc:read"],
  "codeTtlSec": 300,
  "tokenTtlSec": 3600
}
```

响应 `data.clientSecret` **仅展示一次**，请立即保存。

---

## 4. 前端：申请授权码

`POST /client/api/open/codes`

Headers: `Authorization: Bearer {client_access_token}`

```json
{ "appId": "your-app-id" }
```

响应：

```json
{
  "code": 0,
  "data": {
    "code": "auth_code_hex",
    "expiresIn": 300,
    "appId": "your-app-id"
  }
}
```

---

## 5. Python Agent：兑换 Token

`POST /open-api/oauth/token`（无需 JWT）

```json
{
  "grant_type": "authorization_code",
  "client_id": "your-app-id",
  "client_secret": "your-secret",
  "code": "auth_code_from_frontend"
}
```

响应（统一 `Result` 包装）：

```json
{
  "code": 0,
  "data": {
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "scope": "kb:search kb:doc:read",
    "subject": {
      "realm": "client",
      "subjectType": "client_user",
      "subjectId": 123,
      "subjectCode": "CL000001"
    }
  }
}
```

---

## 6. Token 说明

| 项目 | 值 |
|------|-----|
| 类型 | JWT，`realm=OPEN`（与用户端 JWT 分离） |
| 有效期 | 默认 3600 秒，可在 App 配置 |
| scope | 空格分隔，来自 App `defaultScopes` 与 SPI 裁剪结果 |
| 扩展 | JWT claim `ext` 含 `subjectId` 等，供知识库过滤 |

Python 侧建议使用同一 JWT 密钥验签，或后续接入 introspect 端点（Phase 2）。

---

## 7. 常见错误

| 业务码/场景 | 原因 |
|-------------|------|
| 授权码无效或已过期 | code 超时或已兑换 |
| client_secret 无效 | 密钥错误或已轮换 |
| 该应用未授权访问用户域 | App 的 `allowedRealms` 未包含 client |
| 应用已禁用 | 管理端禁用了 App |

---

## 8. 权限码（管理端）

| 权限码 | 说明 |
|--------|------|
| `open-app:list:read` | 查看应用列表 |
| `open-app:create` | 创建应用 |
| `open-app:update` | 更新应用 |
| `open-app:delete` | 删除应用 |
| `open-app:secret:rotate` | 轮换密钥 |

Super Admin 角色自动拥有全部权限。
