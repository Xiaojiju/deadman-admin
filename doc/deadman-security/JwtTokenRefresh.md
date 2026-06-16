# JWT 双令牌与无感刷新

本文档说明 deadman-admin 管理端与用户端（client 组件）的 **Access Token + Refresh Token** 机制、刷新接口用法、安全策略及前端对接建议。

**前端对接专项文档**：[JwtTokenRefresh-Frontend.md](./JwtTokenRefresh-Frontend.md)

---

## 1. 核心概念

| 令牌 | 用途 | 默认时效 | 传递方式 |
|------|------|----------|----------|
| **Access Token** | 业务接口鉴权 | 30 分钟 | 请求头 `Authorization: Bearer {accessToken}` |
| **Refresh Token** | 仅用于换取新的双令牌，不参与业务鉴权 | 7 天 | HttpOnly Cookie（推荐）/ Bearer Header / JSON Body |

JWT Claim 约定：

| Claim | 说明 |
|-------|------|
| `realm` | 端标识：`ADMIN`（管理端）或 `CLIENT`（用户端） |
| `tokenType` | `access` 或 `refresh` |
| `userCode` | 对外用户编码 |
| `sub` | 用户主键 ID |
| `jti` | 令牌唯一标识，用于服务端会话校验与轮换 |

**注册不再签发令牌**：注册成功后仅返回 `RegisterResultVO`（`userCode`、`nickname`），须登录后获取双令牌。

---

## 2. 端（Realm）与接口一览

两套用户体系完全隔离（独立 JWT 密钥、Redis 会话、Cookie）。

### 2.1 管理端（ADMIN）

| 操作 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 注册 | POST | `/api/auth/register` | 公开 |
| 登录 | POST | `/api/auth/login` | 公开 |
| **无感刷新** | POST | `/api/auth/refresh` | 公开（需有效 Refresh Token） |
| 业务接口 | * | `/api/**` | Access Token |

Refresh Cookie：

| 属性 | 值 |
|------|-----|
| 名称 | `deadman_refresh_token` |
| Path | `/api/auth` |
| HttpOnly | `true` |
| SameSite | `Lax` |
| Secure | 由 `deadman.jwt.refresh-cookie-secure` 控制 |

### 2.2 用户端（CLIENT）

| 操作 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 注册 | POST | `/client/api/auth/register` | 公开 |
| 密码登录 | POST | `/client/api/auth/login/password` | 公开 |
| **无感刷新** | POST | `/client/api/auth/refresh` | 公开（需有效 Refresh Token） |
| 业务接口 | * | `/client/api/**` | Access Token |

Refresh Cookie：

| 属性 | 值 |
|------|-----|
| 名称 | `deadman_client_refresh_token` |
| Path | `/client/api/auth` |
| HttpOnly | `true` |
| SameSite | `Lax` |
| Secure | 由 `deadman.component.client.jwt.refresh-cookie-secure` 控制 |

---

## 3. 整体流程

### 3.1 登录 → 持有双令牌

```
用户登录（账号密码等）
        ↓
后端校验通过
        ↓
返回 JSON：accessToken + refreshToken + expiresIn + refreshExpiresIn + userCode + nickname
        ↓
同时 Set-Cookie：HttpOnly Refresh Token
        ↓
前端：accessToken 存内存 / sessionStorage
      refreshToken 优先依赖 Cookie（JS 不可读）
```

### 3.2 业务请求 → Access 过期 → 无感刷新

```
用户发起业务请求
        ↓
请求头携带 Authorization: Bearer {accessToken}
        ↓
后端校验 Access Token
        ├── 有效 → 正常返回业务数据
        └── 过期/无效 → HTTP 401（code: 40100 或 10008）
        ↓
前端 Axios/Fetch 拦截器捕获 401
        ↓
加锁，将并发请求放入队列
        ↓
调用 POST /api/auth/refresh（或用户端对应路径）
        credentials: 'include'  // 携带 Cookie
        ↓
后端 AuthTokenRefreshFilter 处理
        ├── 成功 → 返回新双令牌 + 更新 Cookie
        └── 失败 → 401，见错误码章节
        ↓
前端更新 accessToken，批量重试队列中的请求
        ↓
页面无登录弹窗，用户无感知
```

### 3.3 刷新在后端的处理链（Filter）

刷新请求**不经过 Controller**，由 `AuthTokenRefreshFilter` 统一处理：

1. 按请求路径匹配对应端的 `AuthTokenIssueProvider`（ADMIN / CLIENT）
2. 解析 Refresh Token（Cookie → Authorization → Body）
3. 校验 JWT 签名、realm、tokenType、Redis 会话
4. 原子轮换 Refresh jti，强制替换 Access 会话 jti
5. 返回 `Result<AuthTokenVO>` 并写入新 Cookie

相关类：

| 类 | 职责 |
|----|------|
| `AuthTokenRefreshFilter` | 刷新 HTTP 入口（Filter） |
| `AuthTokenIssueProvider` | 按端签发/刷新扩展点 |
| `AbstractJwtAuthTokenIssueProvider` | JWT 双令牌通用实现 |
| `RealmJwtRefreshTokenStore` | Refresh jti 与用户绑定、轮换、重用检测 |
| `RealmJwtSessionStore` | Access jti 会话校验 |

---

## 4. 接口详细说明

### 4.1 登录成功响应

**管理端** `POST /api/auth/login`  
**用户端** `POST /client/api/auth/login/password`（及其他 Login Provider）

**HTTP 200**

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "refreshExpiresIn": 604800,
    "userCode": "DM202601010001",
    "nickname": "张三"
  },
  "timestamp": 1710000000000
}
```

**响应头（Set-Cookie 示例，管理端）**

```
Set-Cookie: deadman_refresh_token=eyJ...; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Lax
```

> 生产环境 HTTPS 建议开启 `refresh-cookie-secure=true`，Cookie 将带 `Secure` 属性。

---

### 4.2 无感刷新

**管理端** `POST /api/auth/refresh`  
**用户端** `POST /client/api/auth/refresh`

无需 Access Token。Refresh Token 三选一传入（优先级从高到低）：

#### 方式 A：HttpOnly Cookie（推荐）

```http
POST /api/auth/refresh HTTP/1.1
Cookie: deadman_refresh_token=eyJ...
```

前端须设置 `credentials: 'include'`（或 axios `withCredentials: true`）。

#### 方式 B：Authorization Bearer

```http
POST /api/auth/refresh HTTP/1.1
Authorization: Bearer eyJ...refreshToken...
```

#### 方式 C：JSON Body

```http
POST /api/auth/refresh HTTP/1.1
Content-Type: application/json

{
  "refreshToken": "eyJ..."
}
```

**成功响应**：与登录相同结构（新的 `accessToken`、`refreshToken`，Cookie 同步更新）。

**失败响应**：HTTP 401

```json
{
  "code": 10008,
  "msg": "令牌无效或已过期",
  "data": null,
  "timestamp": 1710000000000
}
```

或重用检测：

```json
{
  "code": 10009,
  "msg": "检测到令牌异常重用，请重新登录",
  "data": null,
  "timestamp": 1710000000000
}
```

---

### 4.3 注册（不返回令牌）

**管理端** `POST /api/auth/register`  
**用户端** `POST /client/api/auth/register`

**HTTP 200**

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "userCode": "DM202601010001",
    "nickname": "张三"
  },
  "timestamp": 1710000000000
}
```

注册完成后须调用登录接口获取双令牌。

---

### 4.4 业务接口鉴权

```http
GET /api/auth/permissions HTTP/1.1
Authorization: Bearer eyJ...accessToken...
```

- 仅接受 `tokenType=access` 的 JWT
- Refresh Token 用于业务接口将被拒绝（视为未认证）
- Access 过期返回 HTTP 401，由前端触发刷新流程

---

## 5. 安全策略

### 5.1 Refresh Token 与用户绑定（单活跃 Refresh）

每个用户在每个 realm 下仅保留 **一个当前有效的 Refresh jti**（Redis：`{refreshUserIndexPrefix}{userId}` → jti）。

- 登录 / 刷新成功：覆盖为最新 jti
- 旧 Refresh Token 再次用于刷新：**无效**（`10008`）

### 5.2 刷新轮换与 Access 会话替换

每次成功刷新：

1. Redis Lua 原子操作：`activeJti` 从旧值替换为新值，旧 jti 写入 `used` 集合
2. **强制**更新 Access 会话 jti（`replaceSession`），使上一轮 Access Token 立即失效

避免「一次刷新产生多个同时有效的 Access Token」。

### 5.3 Refresh Token 重用检测（盗用防护）

已成功轮换过的 Refresh jti 会写入 Redis：

```
{refreshKeyPrefix}used:{jti}  →  userId   （TTL = Refresh 有效期）
```

若攻击者持有**已被轮换**的旧 Refresh Token 再次请求刷新：

1. 判定为 `REUSED`
2. **吊销该用户在本端的全部 Access / Refresh 会话**
3. 返回 `10009`，用户须重新登录

服务端会记录 WARN 日志：`检测到 Refresh Token 重用，realm=... userId=... jti=...`

### 5.4 Refresh Token 不可用于业务接口

`JwtAuthenticationFilter` / `ClientJwtAuthenticationFilter` 会拒绝 `tokenType=refresh` 的 JWT。

---

## 6. 错误码

| code | 枚举 | 含义 | 前端建议 |
|------|------|------|----------|
| 0 | SUCCESS | 成功 | — |
| 40100 | UNAUTHORIZED | 未认证（Access 缺失/无效） | 尝试 refresh；仍失败则跳登录 |
| 10008 | TOKEN_INVALID | Refresh 无效或已过期 | 清除 token，跳登录 |
| 10009 | TOKEN_REUSE_DETECTED | Refresh 异常重用，会话已吊销 | **立即**清除 token，跳登录，勿再 refresh |

---

## 7. 配置项

### 7.1 管理端（`application.yaml` / 环境变量）

```yaml
deadman:
  jwt:
    secret: ${DEADMAN_JWT_SECRET}                    # 至少 32 字节
    access-expiration-ms: ${DEADMAN_JWT_ACCESS_EXPIRATION_MS:1800000}      # 30 分钟
    refresh-expiration-ms: ${DEADMAN_JWT_REFRESH_EXPIRATION_MS:604800000}  # 7 天
    refresh-cookie-secure: ${DEADMAN_JWT_REFRESH_COOKIE_SECURE:false}
  security:
    multi-session-enabled: ${DEADMAN_MULTI_SESSION_ENABLED:true}
```

### 7.2 用户端

```yaml
deadman:
  component:
    client:
      jwt:
        secret: ${DEADMAN_CLIENT_JWT_SECRET}
        access-expiration-ms: ${DEADMAN_CLIENT_JWT_ACCESS_EXPIRATION_MS:1800000}
        refresh-expiration-ms: ${DEADMAN_CLIENT_JWT_REFRESH_EXPIRATION_MS:604800000}
        refresh-cookie-secure: ${DEADMAN_CLIENT_JWT_REFRESH_COOKIE_SECURE:false}
      security:
        multi-session-enabled: ${DEADMAN_CLIENT_MULTI_SESSION_ENABLED:true}
```

完整环境变量模板见项目根目录 `.env.example`。

### 7.3 Redis 依赖

Refresh 轮换、重用检测、Access 会话校验均依赖 Redis。Redis 不可用时降级为内存存储（仅适合单实例开发环境）。

---

## 8. 前端对接示例

### 8.1 Axios 无感刷新（管理端）

```typescript
let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

async function refreshAdminToken(): Promise<string> {
  const res = await axios.post('/api/auth/refresh', null, {
    withCredentials: true, // 携带 HttpOnly Cookie
  });
  if (res.data.code !== 0) {
    throw new Error(res.data.msg);
  }
  const accessToken = res.data.data.accessToken;
  // accessToken 存内存或 sessionStorage
  sessionStorage.setItem('admin_access_token', accessToken);
  return accessToken;
}

axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error);
    }

    const code = error.response?.data?.code;
    if (code === 10009) {
      // 重用检测：直接登出
      logout();
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve) => {
        pendingRequests.push((token) => {
          original.headers.Authorization = `Bearer ${token}`;
          resolve(axios(original));
        });
      });
    }

    original._retry = true;
    isRefreshing = true;
    try {
      const newToken = await refreshAdminToken();
      pendingRequests.forEach((cb) => cb(newToken));
      pendingRequests = [];
      original.headers.Authorization = `Bearer ${newToken}`;
      return axios(original);
    } catch {
      logout();
      return Promise.reject(error);
    } finally {
      isRefreshing = false;
    }
  }
);

axios.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('admin_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

用户端将路径改为 `/client/api/auth/refresh`，并使用独立的 accessToken 存储 key 即可。

### 8.2 跨域注意

前后端分离且使用 Cookie 时，后端须：

- `Access-Control-Allow-Credentials: true`
- `Access-Control-Allow-Origin` 为具体域名（不可为 `*`）

---

## 9. 扩展：自定义签发策略

令牌签发通过 `AuthTokenIssueProvider` 扩展，按 `realm` 注册：

| 实现 | realm | 模块 |
|------|-------|------|
| `AdminJwtAuthTokenIssueProvider` | `ADMIN` | deadman-security |
| `ClientJwtAuthTokenIssueProvider` | `CLIENT` | deadman-component-client |

自定义实现可继承 `AbstractJwtAuthTokenIssueProvider`，用 `@Primary` 覆盖默认 Bean，或在 `issue()` / `refresh()` 中按条件切换策略。

---

## 10. 相关源码索引

| 路径 | 说明 |
|------|------|
| `deadman-security/.../jwt/AuthTokenRefreshFilter.java` | 刷新 Filter |
| `deadman-security/.../token/AuthTokenIssueProvider.java` | 签发扩展接口 |
| `deadman-security/.../token/AbstractJwtAuthTokenIssueProvider.java` | 签发/刷新核心逻辑 |
| `deadman-security/.../jwt/RealmJwtRefreshTokenStore.java` | Refresh 存储与重用检测 |
| `deadman-security/.../support/AuthTokenResponseSupport.java` | Cookie 读写与响应封装 |
| `deadman-security/.../vo/auth/AuthTokenVO.java` | 双令牌响应体 |
| `deadman-security/.../constants/AdminAuthConstants.java` | 管理端路径与 Cookie 名 |
| `deadman-component-client/.../constants/ClientAuthConstants.java` | 用户端路径与 Cookie 名 |

OpenAPI 片段（注册/登录）：`doc/deadman-security/AuthController.yaml`、`doc/deadman-component-client/ClientAuthController.yaml`。
