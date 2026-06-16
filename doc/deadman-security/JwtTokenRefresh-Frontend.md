# 前端 JWT 双令牌对接指南

本文档面向前端开发，说明如何对接 deadman-admin 的 **Access Token + Refresh Token** 无感刷新方案。

后端机制详见：[JwtTokenRefresh.md](./JwtTokenRefresh.md)

---

## 1. 对接前必读

### 1.1 两套独立认证，不要混用

| 场景 | API 前缀 | 登录 | 刷新 | Access 存储 key（建议） |
|------|----------|------|------|-------------------------|
| **管理后台** | `/api/**` | `POST /api/auth/login` | `POST /api/auth/refresh` | `admin_access_token` |
| **用户端 / C 端** | `/client/api/**` | `POST /client/api/auth/login/password` 等 | `POST /client/api/auth/refresh` | `client_access_token` |

- 管理端与用户端 JWT **密钥、Cookie、会话互不影响**
- 同一页面若同时调用两套 API，须维护 **两套** Access Token 与拦截器（或两个 axios 实例）

### 1.2 令牌存放建议

| 令牌 | 推荐存放 | 说明 |
|------|----------|------|
| Access Token | 内存变量 / `sessionStorage` | 用于 `Authorization` 请求头；关闭标签页即失效 |
| Refresh Token | **HttpOnly Cookie**（后端自动写入） | JS 无法读取，防 XSS；刷新时 `credentials: 'include'` |

不推荐把 Refresh Token 存 `localStorage`（易受 XSS 窃取）。若必须跨子域或无 Cookie 场景，可退化为 Body/Header 传 Refresh，但安全等级下降。

### 1.3 注册不会返回 Token

注册接口只返回 `{ userCode, nickname }`，**必须再调登录接口**才能拿到双令牌。

---

## 2. 对接清单（Checklist）

- [ ] 登录请求开启 `withCredentials: true` / `credentials: 'include'`，以接收 Refresh Cookie
- [ ] 业务请求自动附加 `Authorization: Bearer {accessToken}`
- [ ] 401 拦截器：排除登录/注册/刷新接口，避免死循环
- [ ] 401 时加锁 + 请求队列，只发起 **一次** refresh
- [ ] refresh 成功：更新 accessToken，重试队列
- [ ] `code === 10009`：立即登出，**禁止**再 refresh
- [ ] 登出：清除 accessToken + 调后端登出（若有）+ 前端跳转登录页
- [ ] 跨域：后端 `Allow-Credentials: true`，Origin 不能为 `*`

---

## 3. 登录对接

### 3.1 管理端登录

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "******" }
```

**成功响应 `data` 结构：**

```typescript
interface AuthTokenVO {
  accessToken: string;
  refreshToken: string;      // 响应体中有，但优先依赖 Cookie
  tokenType: 'Bearer';
  expiresIn: number;         // Access 有效秒数，默认 1800
  refreshExpiresIn: number;  // Refresh 有效秒数，默认 604800
  userCode: string;
  nickname: string;
}
```

**前端处理：**

```typescript
// 1. 保存 Access Token
sessionStorage.setItem('admin_access_token', data.accessToken);

// 2. 可选：记录过期时间，用于主动提前刷新
const expireAt = Date.now() + data.expiresIn * 1000;
sessionStorage.setItem('admin_access_expire_at', String(expireAt));

// 3. Refresh Token 由 Set-Cookie 自动保存，无需 JS 处理
// Cookie 名：deadman_refresh_token，Path：/api/auth
```

### 3.2 用户端登录

```http
POST /client/api/auth/login/password
Content-Type: application/json

{ "username": "user01", "password": "******" }
```

响应结构相同，Cookie 名为 `deadman_client_refresh_token`，Path 为 `/client/api/auth`。

### 3.3 登录请求必须带 credentials

```typescript
await axios.post('/api/auth/login', { username, password }, {
  withCredentials: true,
});
```

否则浏览器不会保存 HttpOnly Refresh Cookie，后续无法无感刷新。

---

## 4. 业务请求对接

所有需鉴权接口：

```http
Authorization: Bearer {accessToken}
```

```typescript
axios.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('admin_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

**注意：**

- 仅 Access Token 放入 Authorization
- 不要把 Refresh Token 当作 Bearer 发给业务接口

---

## 5. 无感刷新（核心）

### 5.1 何时触发 refresh

| 条件 | 是否 refresh |
|------|----------------|
| 业务接口 HTTP 401，且 `code` 为 `40100` / `10008` | 是 |
| `code === 10009`（重用检测） | **否**，直接登出 |
| 请求 URL 为 `/login`、`/register`、`/refresh` | **否**，避免循环 |
| 用户未登录态主动访问需鉴权页 | 跳登录，不 refresh |

### 5.2 刷新接口

```http
POST /api/auth/refresh
```

或用户端：

```http
POST /client/api/auth/refresh
```

**推荐：仅依赖 Cookie，Body 为空**

```typescript
const res = await axios.post('/api/auth/refresh', null, {
  withCredentials: true,
});
```

后端解析 Refresh Token 的优先级：

1. HttpOnly Cookie（推荐）
2. `Authorization: Bearer {refreshToken}`
3. JSON Body：`{ "refreshToken": "..." }`

**成功：** 与登录相同，返回新 `accessToken` / `refreshToken`，Cookie 自动更新。

**失败：** HTTP 401，见第 7 节错误处理。

### 5.3 并发 401 的请求队列

多个接口同时 401 时，只能 **refresh 一次**，其余请求排队等待新 token 后重试：

```
请求 A、B、C 同时 401
    → A 触发 refresh（加锁 isRefreshing = true）
    → B、C 进入 pending 队列
    → refresh 成功，用新 token 依次重试 A、B、C
    → 释放锁
```

### 5.4 完整 Axios 拦截器示例（管理端）

```typescript
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const ACCESS_KEY = 'admin_access_token';
const REFRESH_URL = '/api/auth/refresh';

let isRefreshing = false;
let pendingQueue: Array<(token: string) => void> = [];

function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_KEY);
}

function setAccessToken(token: string): void {
  sessionStorage.setItem(ACCESS_KEY, token);
}

function clearAuth(): void {
  sessionStorage.removeItem(ACCESS_KEY);
  sessionStorage.removeItem('admin_access_expire_at');
  // Cookie 需后端 Clear-Cookie 接口或等其自然过期；前端无法删除 HttpOnly Cookie
}

function redirectToLogin(): void {
  clearAuth();
  window.location.href = '/login';
}

function shouldSkipRefresh(url?: string): boolean {
  if (!url) return true;
  return (
    url.includes('/api/auth/login') ||
    url.includes('/api/auth/register') ||
    url.includes('/api/auth/refresh')
  );
}

async function refreshAccessToken(): Promise<string> {
  const res = await axios.post(REFRESH_URL, null, { withCredentials: true });
  const { code, data, msg } = res.data;
  if (code !== 0 || !data?.accessToken) {
    throw new Error(msg || 'refresh failed');
  }
  setAccessToken(data.accessToken);
  return data.accessToken;
}

// 请求拦截：附加 Access Token
axios.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截：401 无感刷新
axios.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<{ code: number; msg: string }>) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;
    const bizCode = error.response?.data?.code;

    if (status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }

    // 重用检测：全端会话已吊销
    if (bizCode === 10009) {
      redirectToLogin();
      return Promise.reject(error);
    }

    if (shouldSkipRefresh(original.url)) {
      return Promise.reject(error);
    }

    // 已在刷新：排队
    if (isRefreshing) {
      return new Promise((resolve) => {
        pendingQueue.push((token) => {
          original.headers.Authorization = `Bearer ${token}`;
          resolve(axios(original));
        });
      });
    }

    original._retry = true;
    isRefreshing = true;

    try {
      const newToken = await refreshAccessToken();
      pendingQueue.forEach((cb) => cb(newToken));
      pendingQueue = [];
      original.headers.Authorization = `Bearer ${newToken}`;
      return axios(original);
    } catch {
      redirectToLogin();
      return Promise.reject(error);
    } finally {
      isRefreshing = false;
    }
  }
);
```

用户端：复制一份，`ACCESS_KEY`、`REFRESH_URL`、URL 前缀判断改为 `/client/api/auth/...` 即可。

### 5.5 Fetch 示例

```typescript
async function apiFetch(input: RequestInfo, init: RequestInit = {}) {
  const token = sessionStorage.getItem('admin_access_token');
  const headers = new Headers(init.headers);
  if (token) headers.set('Authorization', `Bearer ${token}`);

  let res = await fetch(input, { ...init, headers, credentials: 'include' });

  if (res.status === 401) {
    const body = await res.clone().json().catch(() => ({}));
    if (body.code === 10009) {
      redirectToLogin();
      throw new Error(body.msg);
    }
    const refreshRes = await fetch('/api/auth/refresh', {
      method: 'POST',
      credentials: 'include',
    });
    const refreshBody = await refreshRes.json();
    if (refreshBody.code !== 0) {
      redirectToLogin();
      throw new Error(refreshBody.msg);
    }
    sessionStorage.setItem('admin_access_token', refreshBody.data.accessToken);
    headers.set('Authorization', `Bearer ${refreshBody.data.accessToken}`);
    res = await fetch(input, { ...init, headers, credentials: 'include' });
  }
  return res;
}
```

生产环境建议仍使用「加锁 + 队列」版 Fetch 封装，避免并发 401 多次 refresh。

---

## 6. 主动刷新（可选优化）

除被动等 401 外，可在 Access 即将过期前主动 refresh，减少用户感知到的失败重试：

```typescript
function scheduleProactiveRefresh(expiresInSec: number) {
  // 提前 60 秒刷新
  const delay = Math.max((expiresInSec - 60) * 1000, 0);
  setTimeout(async () => {
    try {
      await refreshAccessToken();
    } catch {
      // 静默失败，等下次业务 401 再处理
    }
  }, delay);
}

// 登录成功后调用
scheduleProactiveRefresh(data.expiresIn);
```

---

## 7. 错误码与前端行为

统一响应体：`{ code, msg, data, timestamp }`

| code | HTTP | 含义 | 前端动作 |
|------|------|------|----------|
| `0` | 200 | 成功 | 正常处理 |
| `40100` | 401 | 未认证（无 token / Access 无效） | 尝试 refresh；失败则登出 |
| `10008` | 401 | Refresh 无效或过期 | 清除 token，跳登录 |
| `10009` | 401 | Refresh 异常重用，服务端已吊销全部会话 | **立即**跳登录，勿再 refresh |

```typescript
function handleAuthError(code: number | undefined): void {
  if (code === 10009 || code === 10008) {
    redirectToLogin();
    return;
  }
  if (code === 40100) {
    // 交给拦截器 refresh；若已在 refresh 流程外，跳登录
  }
}
```

---

## 8. 登出

```typescript
async function logout() {
  clearAuth();
  // 若后端提供登出接口，可调用以 Clear-Cookie
  window.location.href = '/login';
}
```

说明：HttpOnly Refresh Cookie 只能由服务端 `Set-Cookie` 过期或 Clear 删除；纯前端无法主动清除。长期方案是增加 `POST /api/auth/logout` 返回 `Max-Age=0` 的 Cookie。

---

## 9. 跨域与 Cookie

前后端分离且 Cookie 方案时：

**后端（或网关）需配置：**

```
Access-Control-Allow-Origin: https://admin.example.com   # 具体域名，不能 *
Access-Control-Allow-Credentials: true
```

**前端：**

```typescript
axios.defaults.withCredentials = true;
// 或单次：axios.post(url, data, { withCredentials: true })
```

**开发环境：** 使用 Vite/Webpack 代理将 `/api` 转发到 `localhost:8080`，使前后端同源，Cookie 最省事：

```typescript
// vite.config.ts
export default {
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/client/api': 'http://localhost:8080',
    },
  },
};
```

---

## 10. 双端共存（管理端 + 用户端）

若同一 SPA 需同时访问两套 API，建议 **两个 axios 实例**：

```typescript
export const adminHttp = axios.create({ baseURL: '', withCredentials: true });
export const clientHttp = axios.create({ baseURL: '', withCredentials: true });

setupAuthInterceptor(adminHttp, {
  accessKey: 'admin_access_token',
  refreshUrl: '/api/auth/refresh',
  skipPaths: ['/api/auth/login', '/api/auth/register', '/api/auth/refresh'],
});

setupAuthInterceptor(clientHttp, {
  accessKey: 'client_access_token',
  refreshUrl: '/client/api/auth/refresh',
  skipPaths: ['/client/api/auth/register', '/client/api/auth/login', '/client/api/auth/refresh'],
});
```

两套 Cookie 名称不同，可同时存在于浏览器。

---

## 11. 常见问题

### Q1：refresh 一直 401，Cookie 似乎没带上？

- 登录/refresh 是否设置了 `withCredentials: true`
- 是否跨域且未配置 `Allow-Credentials`
- Cookie Path 为 `/api/auth`，仅请求该路径及子路径时携带；若 refresh URL 正确应无问题
- 浏览器是否屏蔽第三方 Cookie

### Q2：refresh 成功后业务请求仍 401？

- 是否用**新** `accessToken` 更新了 Authorization
- 是否误把 `refreshToken` 当作 Access 使用
- 管理端与用户端 token 是否混用

### Q3：多个 Tab 同时刷新会互相踢下线吗？

- 会。Refresh 轮换后旧 Refresh 失效；多 Tab 可能有一个 Tab 用到旧 Refresh 触发 `10009`，全端登出。可选方案：单 Tab 刷新 + `BroadcastChannel` 同步新 token，或接受「最后刷新者生效」。

### Q4：能否只用 localStorage 存 Refresh，不用 Cookie？

- 可以，refresh 时 Body 传 `{ refreshToken }`，但不推荐（XSS 风险）。Cookie 方案需 `withCredentials`。

### Q5：注册后直接进首页为什么 401？

- 注册不返回 token，必须先登录。

---

## 12. TypeScript 类型参考

```typescript
/** 统一响应 */
interface Result<T> {
  code: number;
  msg: string;
  data: T;
  timestamp: number;
}

interface AuthTokenVO {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
  userCode: string;
  nickname: string;
}

interface RegisterResultVO {
  userCode: string;
  nickname: string;
}

type LoginResult = Result<AuthTokenVO>;
type RegisterResult = Result<RegisterResultVO>;
type RefreshResult = Result<AuthTokenVO>;
```

---

## 13. 相关文档

| 文档 | 说明 |
|------|------|
| [JwtTokenRefresh.md](./JwtTokenRefresh.md) | 后端双令牌机制、安全策略、配置 |
| [AuthController.yaml](./AuthController.yaml) | 管理端认证 OpenAPI |
| [ClientAuthController.yaml](../deadman-component-client/ClientAuthController.yaml) | 用户端认证 OpenAPI |
