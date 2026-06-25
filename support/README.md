# Deadman Support 目录

本目录存放**桥接层（Support）模块**：连接核心依赖（`deadman-system`、`deadman-security`）与插件/组件，避免核心模块与插件产生直接耦合。

与 [components/](../components/README.md) 的区别：组件是完整业务域（如 C 端用户体系）；Support 只做**组装与桥接**，不引入新的用户域或独立 API 前缀。

与 [plugins/](../plugins/README.md) 的关系：插件保持与用户体系解耦；Support 在 app 中同时引入插件与核心模块时，提供 admin 等特定场景下的 OAuth 绑定逻辑。

## 模块一览

| 模块 | 配置前缀 | 说明 |
|------|----------|------|
| [deadman-support-wechat](#deadman-support-wechat) | `deadman.support.wechat` | 管理端微信小程序登录：未绑定时返回临时令牌，用户名密码认证后自动绑定 openid |
| [deadman-support-admin-im](#deadman-support-admin-im) | `deadman.support.admin-im` | 管理端与腾讯云 IM 桥接：UserSig 签发与用户映射查询 |

## 插拔方式

1. 在根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>` 中登记。
2. 在 `deadman-app/pom.xml` 中引入依赖。
3. 配置开关：`deadman.support.wechat.enabled`（默认 `true`，需 classpath 同时存在 wechat 插件）。

---

## deadman-support-wechat

管理端（admin）微信小程序登录桥接。与 client 组件不同，**管理端不会自动开通 OAuth 用户**，未绑定时需二次认证。

### 登录流程

```
wx.login code
    │
    ▼
POST /api/auth/wechat-miniprogram
    │
    ├─ openid 已绑定 ──► 返回 JWT（与密码登录相同）
    │
    └─ openid 未绑定 ──► code2session 写入 Redis
                         返回 bindToken + expiresIn
                              │
                              ▼
              POST /api/auth/wechat-miniprogram/bind
              { bindToken, username, password }
                              │
                              ▼
              校验密码 → 绑定 openid → 返回 JWT
```

### 配置示例

```yaml
deadman:
  support:
    wechat:
      enabled: true
      bind-token-ttl: 10m
  plugin:
    wechat-miniprogram:
      enabled: true
      login-bindings:
        - group-id: admin
```

### 主要接口

| 方法 | 路径 | 认证 |
|------|------|------|
| POST | `/api/auth/wechat-miniprogram` | 公开 |
| POST | `/api/auth/wechat-miniprogram/bind` | 公开 |

OpenAPI 定义：[openapi/AdminWechatAuth.yaml](deadman-support-wechat/openapi/AdminWechatAuth.yaml)

### 依赖方向

```
deadman-common ← deadman-core ← deadman-system ← deadman-security
                                                      ↑
                              deadman-plugin-wechat ─┘
                                      ↑
                         deadman-support-wechat（桥接）
```

Support 模块**可以**依赖 `deadman-system`（与 components 不同），因其职责就是连接管理端用户表与微信插件。

微信 LoginProvider 在启动时的两阶段注册与覆盖机制见：[doc/deadman-plugin-wechat/WechatLoginProviderRegistration.md](../doc/deadman-plugin-wechat/WechatLoginProviderRegistration.md)。

---

## deadman-support-admin-im

管理端（admin）与腾讯云 IM 插件桥接，`realmId = admin`。与 [deadman-support-client-im](../support/deadman-support-client-im/) 可**同时启用**，分别服务管理端与 C 端用户域。

### 主要接口

| 方法 | 路径 | 认证 |
|------|------|------|
| GET | `/api/im/credential` | 管理端 JWT |
| GET | `/api/im/users/lookup?realm=&subjectId=` | 管理端 JWT |

`/users/lookup` 供客服场景查询对端 IM UserID，例如 `realm=client&subjectId=CL20260001`。

### 配置示例

```yaml
deadman:
  support:
    admin-im:
      enabled: true
  plugin:
    im-tencent:
      enabled: true
```

### 依赖方向

```
deadman-plugin-im-tencent
         ↑
deadman-support-admin-im ← deadman-system / deadman-security
```

