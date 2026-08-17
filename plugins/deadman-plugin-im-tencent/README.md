# deadman-plugin-im-tencent 腾讯云 IM 插件

腾讯云 IM 单插件实现：UserSig 签发、账号同步、`ImUserRealmBridge` 用户域桥接 SPI。

> **测试状态：** 已实现 Mock 网关与单元测试；生产 SecretKey 联调需自行验证。

**Client / Admin 完整接入文档（推荐阅读）：**  
[doc/deadman-plugin-im-tencent/ImClientAdminIntegration.md](../../doc/deadman-plugin-im-tencent/ImClientAdminIntegration.md)

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
    file:
      # 本地存储相对路径 /files/... 拼成绝对 URL，供 IM FaceUrl 等外部系统使用
      public-base-url: ${DEADMAN_FILE_PUBLIC_BASE_URL:}
    im-tencent:
      enabled: true
      mock-enabled: true
      sdk-app-id: ${TENCENT_IM_SDK_APP_ID:}
      secret-key: ${TENCENT_IM_SECRET_KEY:}
      admin-identifier: administrator
      user-sig-expire-seconds: 86400
      # 签名 FaceUrl 刷新间隔（秒）；CDN 稳定链也可接受周期重推
      face-url-refresh-seconds: 1800
      user-id-template: "{realm}_{subjectId}"
  support:
    client-im:
      enabled: true
```

头像同步约定：IM 插件**不**依赖 `FileService`；由 Support（`admin-im` / `client-im`）先 `resolveAccessUrl(avatarFileId)`，再把公网绝对 URL + fileId 传入 `ImUserProfileSource`。
### 4. 前端

详见 [ImClientAdminIntegration.md](../../doc/deadman-plugin-im-tencent/ImClientAdminIntegration.md)。摘要：

```
# 用户端（CLIENT JWT）
GET /client/api/im/credential
→ { sdkAppId, imUserId, userSig, expireAt }
→ TIM SDK login

# 管理端（Admin JWT）
GET /api/im/credential
→ 同上，登录管理端 IM 账号

GET /api/im/users/lookup?realm=client&subjectId={userCode}
→ { realmId, subjectId, imUserId }  # 客服定位对端
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
