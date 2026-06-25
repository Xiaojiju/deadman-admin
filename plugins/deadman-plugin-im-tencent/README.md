# deadman-plugin-im-tencent 腾讯云 IM 插件

腾讯云 IM 单插件实现：UserSig 签发、账号同步、`ImUserRealmBridge` 用户域桥接 SPI。

> **测试状态：** 已实现 Mock 网关与单元测试；生产 SecretKey 联调需自行验证。

## 职责

| 能力 | 说明 |
|------|------|
| UserSig 签发 | 后端持有 SecretKey，前端仅拿凭证登录 TIM SDK |
| 账号同步 | 调用腾讯云 `account_import` 导入/更新昵称头像 |
| 用户映射 | 表 `plugin_im_user_account`：`realm + subjectId → imUserId` |
| 用户域桥接 | `ImUserRealmBridge` SPI，与各用户体系解耦 |

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-im-tencent</artifactId>
</dependency>
<!-- C 端桥接（按需） -->
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-client-im</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-support-admin-im</artifactId>
</dependency>
```

### 2. 初始化数据库

执行 `src/main/resources/db/im-tencent/schema.sql`。

### 3. 配置

```yaml
deadman:
  plugin:
    im-tencent:
      enabled: true
      mock-enabled: true
      sdk-app-id: ${TENCENT_IM_SDK_APP_ID:}
      secret-key: ${TENCENT_IM_SECRET_KEY:}
      admin-identifier: administrator
      user-sig-expire-seconds: 86400
      user-id-template: "{realm}_{subjectId}"
  support:
    client-im:
      enabled: true
```

### 4. 前端

```
GET /client/api/im/credential  （需 CLIENT JWT）
→ { sdkAppId, imUserId, userSig, expireAt }
→ TIM SDK login
```

## 扩展新用户域

实现 `ImUserRealmBridge` 并注册为 Spring Bean，可选新增 Support 模块暴露 REST：

```java
@Component
public class AdminImUserRealmBridge implements ImUserRealmBridge {
    @Override
    public String realmId() { return "admin"; }
    // resolveCurrentSubject / resolveProfileSource ...
}
```

## 依赖约定

- 插件仅依赖 `deadman-common`、`deadman-core`
- **禁止**依赖 `deadman-system`、`deadman-component-client`
- Support 模块负责桥接具体用户域
