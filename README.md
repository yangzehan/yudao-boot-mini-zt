<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-blue.svg" alt="Downloads">
 <img src="https://img.shields.io/badge/Flink-1.18+-blue.svg" alt="Flink">
 <img src="https://img.shields.io/github/license/YunaiV/ruoyi-vue-pro"/>
</p>

# Yudao Boot Mini - Flink 实时计算平台

**芋道源码** 实时计算平台，基于 Apache Flink 提供完整的作业管理与部署支持。

## 🚀 Flink 路线图

本项目致力于提供完整的 Apache Flink 作业管理与部署支持，打造一站式实时计算平台。

### 📊 版本支持路线图

| 版本 | 状态 | 作业类型支持 | 部署模式支持 |
|------|------|-------------|-------------|
| **Flink 1.18** | ✅ 已完成 | SQL / JAR / CDC | Local / Remote / Yarn Application |
| **Flink 1.19** | 🔄 开发中 | SQL / JAR / CDC | Local / Remote / Yarn Application |
| **Flink 1.20** | 📅 计划中 | SQL / JAR / CDC | Local / Remote / Yarn Application / Yarn Session / Kubernetes |
| **Flink 最新版** | 📅 规划中 | SQL / JAR / CDC | 全模式支持 |

### ✅ 已完成功能

#### Flink 1.18 支持
- **作业类型**
  - `SQL 作业`：支持直接提交 SQL 脚本，实时处理数据流
  - `JAR 作业`：支持提交自定义 Flink JAR 包，灵活扩展业务逻辑
  - `CDC 作业`：支持 Change Data Capture，实现数据库实时同步
- **部署模式**
  - `Local`：本地开发调试模式，快速验证作业逻辑
  - `Remote`：连接远程 Flink 集群，适合测试环境
  - `Yarn Application`：提交到 Yarn 集群运行，生产环境推荐

### 🔄 开发中功能

#### Flink 1.19 支持
- [ ] 完成 Flink 1.19 版本适配
- [ ] 验证 SQL / JAR / CDC 作业类型兼容性
- [ ] 验证 Local / Remote / Yarn Application 部署模式
- [ ] 更新依赖版本与配置模板

### 📋 计划中功能

#### Flink 1.20 支持
- [ ] 完成 Flink 1.20 版本适配
- [ ] 新增 Yarn Session 部署模式支持
- [ ] 新增 Kubernetes 部署模式支持
- [ ] 完善作业监控与告警功能

#### 长期规划
- [ ] 多版本 Flink 集群统一管理
- [ ] 作业版本管理与回滚
- [ ] 作业性能调优建议
- [ ] 与上游数据源深度集成
- [ ] 作业血缘与依赖管理

### 🏗️ 技术架构

```
yudao-module-flink/          # Flink 模块父目录
├── yudao-flink-common/      # Flink 公共依赖模块
├── yudao-flink-118-service/ # Flink 1.18 版本微服务
├── yudao-flink-119-service/ # Flink 1.19 版本微服务（开发中）
├── yudao-flink-120-service/ # Flink 1.20 版本微服务（计划中）
└── ...
```

### 📝 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|---------|
| 2025.01 | v1.0 | 完成 Flink 1.18 支持（SQL/JAR/CDC + Local/Remote/Yarn Application） |
| - | v1.1 | 开发 Flink 1.19 支持（进行中） |

> 📌 **提示**：如需了解更多 Flink 相关开发细节，请参考项目内 `yudao-flink-*` 相关模块文档。

---

## 📦 模块结构

| 模块 | 说明 |
|------|------|
| `yudao-dependencies` | Maven 依赖版本管理 |
| `yudao-framework` | Java 框架拓展 |
| `yudao-server` | 主应用容器 |
| `yudao-module-system` | 系统功能模块 |
| `yudao-module-infra` | 基础设施模块 |
| `yudao-module-data-studio` | 数据工作室模块 |
| `yudao-module-flink` | Flink 实时计算模块（含 1.18/1.19/1.20 服务） |

## 📄 开源协议

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源协议。

## 🤝 致谢

- [芋道源码](https://www.iocoder.cn/) - 项目基于 Yudao Boot Mini 构建
- [Apache Flink](https://flink.apache.org/) - 实时计算引擎
