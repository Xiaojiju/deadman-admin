# Deadman Extensions 目录

本目录存放**能力延伸（Extension）模块**：定义 SPI 契约、统一编排与持久化，**不包含**具体渠道/存储后端的实现。

与 [plugins/](../plugins/README.md) 的分工：

| 目录 | 职责 | 示例 |
|------|------|------|
| **extensions/** | 主能力：SPI、门面 Service、统一数据模型、自动配置 | `deadman-extension-pay`、`deadman-extension-file` |
| **plugins/** | 具体实现：渠道 API、存储 Provider、回调 Controller | `deadman-plugin-pay-wechat`、`deadman-plugin-storage-oss` |

与 [support/](../support/README.md) 的区别：Support 是核心与插件/组件之间的**桥接层**；Extension 是**可复用的业务能力主体**，可被多个插件实现依赖。

## 模块一览

| 模块 | 配置前缀 | 说明 |
|------|----------|------|
| [deadman-extension-pay](deadman-extension-pay/) | `deadman.plugin.pay` | 支付 SPI、`PayService`、统一订单表 |
| [deadman-extension-file](deadman-extension-file/) | `deadman.plugin.file` | 文件 SPI、`FileService`、元数据表与 REST API |

## 插拔方式

1. 在根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>` 中登记 extension 模块。
2. 在 `deadman-app/pom.xml` 中引入 **extension + 对应 plugin 实现**。

**支付示例：**

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-extension-pay</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-pay-wechat</artifactId>
</dependency>
```

**文件示例：**

```xml
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-extension-file</artifactId>
</dependency>
<dependency>
    <groupId>com.mtfm</groupId>
    <artifactId>deadman-plugin-storage-local</artifactId>
</dependency>
```

3. 仅引入 extension 而不引入任何实现 plugin 时，对应 Provider 列表为空，业务调用会失败。

## 依赖约定

| 允许 | 禁止 |
|------|------|
| extension 依赖 `deadman-common`、`deadman-core`（file 另依赖 security 做权限） | extension 依赖具体 plugin 实现 |
| plugin 依赖 extension | plugin 与 extension 形成环依赖 |
| 业务 / app 同时依赖 extension + plugin | 在 plugin 中重复定义 extension 已有实体 |

详细文档见各 extension 子模块 README。
