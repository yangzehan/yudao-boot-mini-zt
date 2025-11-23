# CLAUDE.md

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with the **Yudao Boot Mini (芋道快速开发平台精简版)** repository.

## 项目概览 (Project Overview)

**Yudao Boot Mini** 是基于 Spring Boot 的 Java 快速开发平台精简版，专注于核心系统功能和基础设施。它是完整版 Yudao 平台的精简版本，去除了会员中心、工作流、支付、商城等业务模块，以提升开发和编译速度。

**当前状态：** 这是"精简版"项目，仅启用了核心模块。其他业务模块（会员、工作流、支付、商城等）在配置中被注释掉了。

### 项目信息
- **版本标识**: `2025.10-jdk8-SNAPSHOT`
- **Java 版本**: JDK 8
- **Spring Boot**: 2.7.18
- **架构类型**: 多模块 Maven 架构
- **数据库支持**: MySQL 5.7/8.0+（也支持 Oracle、PostgreSQL、SQL Server 等）
- **开源协议**: MIT License

## 快速开始 (Quick Start)

### 前置条件
- **Java 8+** (推荐使用 JDK 8)
- **MySQL 5.7+** 或更高版本
- **Redis 5.0+** 或更高版本
- **Maven Daemon (mvnd)** - 项目使用 mvnd 提升构建速度

### 安装与运行

#### 1. 克隆并安装依赖
```bash
# 进入项目目录
cd yudao-boot-mini-zt

# 使用 mvnd 安装依赖（推荐）
mvnd clean install -DskipTests
```

#### 2. 数据库初始化
```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE ruoyi-vue-pro DEFAULT CHARACTER SET utf8mb4;"

# 2. 导入 SQL 脚本
mysql -u root -p ruoyi-vue-pro < sql/create_table.sql
mysql -u root -p ruoyi-vue-pro < sql/datastudio_datasource.sql
mysql -u root -p ruoyi-vue-pro < sql/mysql/V20240720_202407_SQL-update_20240722.sql
mysql -u root -p ruoyi-vue-pro < sql/mysql/V20241017_202410_SQL-update_20241018.sql
# ...（根据需要导入更多迁移脚本）
```

#### 3. 修改配置
编辑 `yudao-server/src/main/resources/application-local.yaml`，修改数据库和 Redis 连接信息：
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai
          username: your_username
          password: your_password
  redis:
    host: 127.0.0.1
    port: 6379
    password: your_redis_password
```

#### 4. 启动应用
```bash
# 启动开发环境
mvnd spring-boot:run -Dspring-boot.run.profiles=local

# 或者使用 IDE 启动 YudaoServerApplication.java
```

#### 5. 访问系统
- **API 文档**: <http://localhost:8083/swagger-ui/index.html>
- **Druid 监控**: <http://localhost:8083/druid/index.html>
- **Spring Boot Admin**: <http://localhost:8083/admin>

## 构建系统 (Build System)

### ⚠️ 重要：使用 mvnd 而非 mvn

本项目使用 **Maven Daemon (mvnd)** 以获得更快的构建速度。**请务必使用 `mvnd` 命令而非 `mvn`**。

### 常用命令

```bash
# 构建整个项目
mvnd clean compile

# 运行测试
mvnd test

# 打包应用（跳过测试）
mvnd clean package -DskipTests

# 运行单个测试
mvnd test -Dtest=ClassName#methodName

# 安装依赖
mvnd clean install

# 启动应用
mvnd spring-boot:run

# 构建指定模块
mvnd clean compile -pl yudao-module-system

# 运行指定模块的测试
mvnd test -pl yudao-module-system

# 清理构建缓存
mvnd clean
```

### 开发环境配置 (Spring Profiles)

项目使用 Spring Profiles 管理不同环境配置：

- **`local`** - 本地开发环境（默认端口：8083）
- **`dev`** - 开发环境
- **`test`** - 测试环境
- **`prod`** - 生产环境

## 项目架构 (Project Architecture)

### 模块结构 (Module Structure)

项目遵循**多模块 Maven 架构**，清晰的模块划分：

#### 核心模块

```
yudao-boot-mini-zt/
├── yudao-dependencies/           # Maven 依赖版本管理（BOM）
├── yudao-framework/              # 核心框架组件和 Spring Boot Starters
├── yudao-server/                 # 主应用容器（聚合所有模块）
├── yudao-module-system/          # 系统功能模块（用户、角色、权限、字典等）
├── yudao-module-infra/           # 基础设施模块（代码生成、文件管理、监控等）
└── yudao-module-datastudio/      # 数据工作室模块
```

#### 业务模块（当前注释状态）

以下模块在 `pom.xml` 中已被注释，仅保留 `yudao-module-system`、`yudao-module-infra` 和 `yudao-module-datastudio`：

```xml
<!-- 可选业务模块（当前被注释） -->
<!-- yudao-module-member       # 会员中心 -->
<!-- yudao-module-bpm          # 工作流引擎 -->
<!-- yudao-module-pay          # 支付系统 -->
<!-- yudao-module-mp           # 微信公众号 -->
<!-- yudao-module-mall         # 商城系统 -->
<!-- yudao-module-crm          # CRM 客户关系管理 -->
<!-- yudao-module-erp          # ERP 企业资源规划 -->
<!-- yudao-module-ai           # AI 大模型 -->
<!-- yudao-module-report       # 报表系统 -->
```

### 包结构 (Package Organization)

每个模块遵循标准的包结构约定：

```java
cn.iocoder.yudao.module.{module-name}/
├── api/                          # API 接口（供其他模块调用）
│   └── {sub-module}/             # 子模块
├── controller/                   # REST 控制器
│   ├── admin/                    # 管理后台接口
│   └── app/                      # 用户 App 接口
├── service/                      # 业务逻辑层
│   ├── {sub-module}/             # 子模块
│   │   ├── impl/                 # Service 实现类
│   │   └── Service.java          # Service 接口
├── convert/                      # 对象转换（MapStruct）
├── dal/                          # 数据访问层
│   ├── dataobject/               # 实体类（DO - Data Object）
│   └── mysql/                    # MyBatis Mapper 接口
├── enums/                        # 枚举类定义
├── framework/                    # 模块特定框架代码
├── job/                          # 定时任务
├── mq/                           # 消息队列
├── websocket/                    # WebSocket 支持
└── util/                         # 工具类
```

### 框架组件 (Framework Components)

`yudao-framework` 模块提供以下 Spring Boot Starters：

#### 核心框架 Starters
- **`yudao-spring-boot-starter-web`** - Web MVC、API 日志、异常处理
- **`yudao-spring-boot-starter-security`** - JWT 认证与授权
- **`yudao-spring-boot-starter-mybatis`** - MyBatis Plus 数据库访问
- **`yudao-spring-boot-starter-redis`** - Redis 集成与缓存
- **`yudao-spring-boot-starter-job`** - Quartz 定时任务
- **`yudao-spring-boot-starter-mq`** - 消息队列集成（Kafka、RabbitMQ、RocketMQ）
- **`yudao-spring-boot-starter-websocket`** - WebSocket 通信

#### 业务框架 Starters
- **`yudao-spring-boot-starter-biz-tenant`** - 多租户支持
- **`yudao-spring-boot-starter-biz-data-permission`** - 数据级权限控制
- **`yudao-spring-boot-starter-biz-ip`** - 基于 IP 的访问控制

#### 基础设施 Starters
- **`yudao-spring-boot-starter-monitor`** - 应用监控（Spring Boot Admin、SkyWalking）
- **`yudao-spring-boot-starter-protection`** - 服务保护（分布式锁、限流、幂等）
- **`yudao-spring-boot-starter-excel`** - Excel 导入/导出功能

#### 通用组件
- **`yudao-common`** - 通用工具类和共享组件

## 开发指南 (Development Guidelines)

### 数据库操作 (Database Operations)

#### 数据访问模式
- **ORM 框架**: MyBatis Plus 3.5.7
- **软删除**: 使用 `deleted` 字段实现软删除
- **多租户**: 默认启用，通过 TenantLineHandler 实现数据隔离
- **实体类位置**: `dal/dataobject/` 包下
- **Mapper 接口位置**: `dal/mysql/` 包下

#### 多租户说明
多租户功能通过 `TenantLineHandler` 实现，默认开启：
- 配置文件: `src/main/java/cn/iocoder/yudao/framework/tenant/core/db/TenantDatabaseInterceptor.java`
- 租户隔离: 基于数据库字段 `tenant_id` 实现数据隔离
- 默认启用，如需禁用修改 `yudao.tenant.enable` 配置

### API 开发 (API Development)

#### 设计规范
- **RESTful 设计**: 遵循 REST 风格设计接口
- **统一响应**: 使用 `CommonResult<T>` 封装响应结果
- **控制器分离**: `admin/`（管理后台）和 `app/`（用户应用）
- **对象转换**: 使用 MapStruct 在各层之间转换
- **参数校验**: 使用 Bean Validation 注解

#### 示例控制器
```java
@RestController
@RequestMapping("/admin-api/system/user")
@Validated
public class UserController {

    @GetMapping("/page")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO reqVO) {
        return success(userService.getUserPage(reqVO));
    }
}
```

### 代码生成器 (Code Generator)

项目内置代码生成器位于 `yudao-module-infra` 模块，用于快速生成 CRUD 代码。

#### 生成功能
- **后端**: Java 实体类、Mapper、Service、Controller
- **前端**: Vue3 管理后台代码
- **SQL**: 数据库脚本
- **测试**: 单元测试（可选）

#### 使用方式
1. 在管理后台的"代码生成"页面配置
2. 选择数据库表
3. 配置生成选项
4. 下载生成的代码

### 安全与权限 (Security & Permissions)

#### 认证方式
- **JWT**: 基于 Token 的无状态认证
- **多终端支持**: 支持 Web、App、小程序等多种终端
- **SSO 单点登录**: 支持 OAuth2 授权码模式

#### 权限控制
- **RBAC**: 基于角色的访问控制（Role-Based Access Control）
- **数据权限**: 支持行级和列级数据权限控制
- **菜单权限**: 支持动态菜单和按钮级权限控制
- **多租户**: 支持租户级别的数据隔离

### 日志系统 (Logging)

#### 日志分类
- **访问日志**: 记录 API 调用历史（`yudao_module_infra_api_access_log`）
- **错误日志**: 记录系统异常信息（`yudao_module_infra_api_error_log`）
- **操作日志**: 记录用户操作（`yudao_module_system_operate_log`）
- **登录日志**: 记录用户登录历史（`yudao_module_system_login_log`）

#### 日志级别配置
在 `application-local.yaml` 中可配置不同模块的日志级别：
```yaml
logging:
  level:
    cn.iocoder.yudao.module.system.dal.mysql: DEBUG
    cn.iocoder.yudao.module.infra.dal.mysql: DEBUG
```

## 配置详解 (Configuration Details)

### 核心配置文件

- **`yudao-server/src/main/resources/application.yaml`** - 主配置（跨环境共享）
- **`yudao-server/src/main/resources/application-local.yaml`** - 本地开发环境
- **`yudao-server/src/main/resources/application-dev.yaml`** - 开发环境

### 关键配置项

#### 数据库配置
```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro
          username: root
          password: mysql_Fe3StE
```

#### Redis 配置
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    password: redis_Xzt5Ea
```

#### 多租户配置
```yaml
yudao:
  tenant:
    enable: true    # 是否启用多租户
    ignore-urls:    # 忽略 URL（无需验证租户）
      - /jmreport/*
```

#### 任务调度配置
```yaml
spring:
  quartz:
    auto-startup: true
    scheduler-name: schedulerName
    job-store-type: jdbc
```

### 环境端口配置
- **Local 环境**: 端口 8083
- **Dev 环境**: 端口 8080（默认）
- **Prod 环境**: 端口 8080（默认）

## 测试 (Testing)

### 测试框架
- **JUnit 5** - Java 单元测试框架
- **Mockito** - Mock 框架
- **Spring Boot Test** - 集成测试支持
- **Embedded Redis** - 测试时内嵌 Redis

### 测试结构
- 测试类遵循与主代码相同的包结构
- 使用 `@SpringBootTest` 进行集成测试
- 使用 `@ExtendWith(MockitoExtension.class)` 进行单元测试
- 测试数据存放在 `src/test/resources` 目录

### 运行测试
```bash
# 运行所有测试
mvnd test

# 运行指定模块测试
mvnd test -pl yudao-module-system

# 运行单个测试类
mvnd test -Dtest=UserServiceTest

# 运行单个测试方法
mvnd test -Dtest=UserServiceTest#createUser

# 生成测试覆盖率报告
mvnd jacoco:report
```

### Mock 开发
本地开发环境默认启用 Mock 认证：
```yaml
yudao:
  security:
    mock-enable: true
```

## 数据库迁移 (Database Migration)

### 初始化数据库
1. **创建数据库**:
   ```sql
   CREATE DATABASE ruoyi-vue-pro DEFAULT CHARACTER SET utf8mb4;
   ```

2. **执行初始化脚本**:
   ```bash
   # 基础表结构
   mysql -u root -p ruoyi-vue-pro < sql/create_table.sql

   # 数据工作室相关表
   mysql -u root -p ruoyi-vue-pro < sql/datastudio_datasource.sql

   # 其他迁移脚本（按时间顺序）
   mysql -u root -p ruoyi-vue-pro < sql/mysql/V20240720_202407_SQL-update_20240722.sql
   mysql -u root -p ruoyi-vue-pro < sql/mysql/V20241017_202410_SQL-update_20241018.sql
   ```

### 迁移脚本规范
- **命名规范**: `V{YYYYMMDD}__{YYYYMM}_SQL-update_{YYYYMMDD}.sql`
- **执行顺序**: 按文件名数字顺序执行
- **存储位置**: `sql/mysql/` 目录
- **版本管理**: 确保每个环境使用相同版本的脚本

### 支持的数据库
- **MySQL** (推荐 5.7/8.0+)
- **Oracle** (11g+)
- **PostgreSQL** (9.1+)
- **SQL Server** (2012+)
- **达梦 DM** (DM8)
- **人大金仓 KingbaseES** (8.0+)
- **OpenGauss** (3.0+)

## 常见开发任务 (Common Development Tasks)

### 添加新模块

#### 步骤 1: 创建模块目录和 POM
```bash
# 在根目录创建新模块
mkdir yudao-module-{name}
# 创建标准包结构
mkdir -p yudao-module-{name}/src/main/java/cn/iocoder/yudao/module/{name}
```

#### 步骤 2: 配置根 POM
```xml
<!-- 在根 pom.xml 中添加 -->
<module>yudao-module-{name}</module>
```

#### 步骤 3: 创建标准包结构
```
yudao-module-{name}/
├── pom.xml
└── src/main/java/cn/iocoder/yudao/module/{name}/
    ├── api/
    ├── controller/
    ├── service/
    ├── convert/
    ├── dal/
    │   ├── dataobject/
    │   └── mysql/
    ├── enums/
    └── framework/
```

#### 步骤 4: 配置主模块依赖
在 `yudao-server/pom.xml` 中添加：
```xml
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-module-{name}</artifactId>
    <version>${revision}</version>
</dependency>
```

### 添加新实体

#### 步骤 1: 创建实体类
```java
@TableName("{table_name}")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class {EntityName}DO extends BaseDO {
    // 字段定义
}
```

#### 步骤 2: 创建 Mapper
```java
@Mapper
public interface {EntityName}Mapper extends BaseMapper2<{EntityName}DO> {
}
```

#### 步骤 3: 创建 Service
```java
@Service
@Slf4j
public class {EntityName}Service {
    // 业务方法
}
```

#### 步骤 4: 创建 Controller
```java
@RestController
@RequestMapping("/admin-api/{module}/{name}")
public class {EntityName}Controller {
    // REST 接口
}
```

### 启用业务模块

如需启用被注释的业务模块：

1. **取消根 POM 注释** (`yudao-boot-mini-zt/pom.xml`)
2. **取消 Server POM 注释** (`yudao-server/pom.xml`)
3. **导入对应数据库脚本** (`sql/mysql/`)
4. **添加相关配置**（如需要）

示例：
```xml
<!-- 启用会员模块 -->
<module>yudao-module-member</module>

<!-- 在 yudao-server/pom.xml 中取消注释 -->
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-module-member</artifactId>
    <version>${revision}</version>
</dependency>
```

## 故障排除 (Troubleshooting)

### 常见问题

#### 1. 构建失败
**问题**: `mvnd: command not found`
```bash
# 解决方案：安装 Maven Daemon
# macOS
brew install mvndaemon

# Windows (使用 Chocolatey)
choco install mvnd

# 或使用普通 Maven
mvn clean install
```

#### 2. 组件未找到错误
**问题**: `Bean not found`
```yaml
# 解决方案：检查包扫描配置
# 确保 application.yaml 中包含
yudao:
  info:
    base-package: cn.iocoder.yudao
```

#### 3. 数据库连接失败
**问题**: `Connection refused`
```yaml
# 解决方案：验证 application-local.yaml 配置
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro
          username: your_username
          password: your_password
```

#### 4. 权限问题
**问题**: `Access is denied`
```bash
# 解决方案：检查用户角色和权限分配
# 1. 确认用户已分配角色
# 2. 确认角色已分配菜单权限
# 3. 检查数据权限配置
```

#### 5. Redis 连接失败
**问题**: `Connection timed out`
```yaml
# 解决方案：检查 Redis 配置
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: redis_password  # 如设置了密码
```

#### 6. 端口占用
**问题**: `Port 8083 was already in use`
```bash
# 解决方案 1：修改端口
# 在 application-local.yaml 中配置
server:
  port: 8084

# 解决方案 2：杀死占用进程
# macOS/Linux
lsof -ti:8083 | xargs kill -9

# Windows
netstat -ano | findstr :8083
taskkill /PID <pid> /F
```

#### 7. 多租户数据隔离失效
**问题**: 用户能看到其他租户数据
```yaml
# 解决方案：确保多租户配置正确
yudao:
  tenant:
    enable: true

# 检查 TenantLineHandler 是否正确配置
# 位置：src/main/java/cn/iocoder/yudao/framework/tenant/core/db/
```

### 获取帮助

- **官方文档**: <https://doc.iocoder.cn/>
- **快速开始**: <https://doc.iocoder.cn/quick-start/>
- **视频教程**: <https://doc.iocoder.cn/video/>
- **演示地址**: <http://dashboard-vue3.yudao.iocoder.cn>
- **GitHub**: <https://github.com/YunaiV/ruoyi-vue-pro>
- **Gitee**: <https://gitee.com/yudaocode/yudao-boot-mini>

### 调试技巧

#### 1. 启用 DEBUG 日志
```yaml
# application-local.yaml
logging:
  level:
    root: DEBUG
    cn.iocoder.yudao: DEBUG
```

#### 2. 查看 SQL 执行日志
```yaml
logging:
  level:
    cn.iocoder.yudao.module.system.dal.mysql: DEBUG
```

#### 3. 使用 Druid 监控
- 访问: <http://localhost:8083/druid/index.html>
- 查看 SQL 执行情况、慢查询、性能统计

#### 4. 使用 Spring Boot Admin
- 访问: <http://localhost:8083/admin>
- 查看应用状态、内存使用、线程信息

## 技术栈 (Technology Stack)

### 核心技术

| 技术 | 说明 | 版本 |
|------|------|------|
| **Java** | 应用开发语言 | 8+ |
| **Spring Boot** | 应用开发框架 | 2.7.18 |
| **Spring Framework** | 核心框架 | 5.3.24 |
| **Maven** | 构建工具 | 3.9+ |
| **Maven Daemon** | 快速构建工具 | Latest |

### 数据存储

| 技术 | 说明 | 版本 |
|------|------|------|
| **MySQL** | 关系型数据库 | 5.7 / 8.0+ |
| **MyBatis Plus** | ORM 框架 | 3.5.7 |
| **Redis** | 缓存数据库 | 5.0 / 6.0 / 7.0 |
| **Redisson** | Redis 客户端 | 3.32.0 |
| **Druid** | 连接池 | 1.2.23 |
| **Dynamic Datasource** | 动态数据源 | 3.6.1 |

### 安全与认证

| 技术 | 说明 | 版本 |
|------|------|------|
| **Spring Security** | 安全框架 | 5.7.11 |
| **JWT** | Token 认证 | - |
| **Hibernate Validator** | 参数校验 | 6.2.5 |

### 开发工具

| 技术 | 说明 | 版本 |
|------|------|------|
| **Lombok** | 减少样板代码 | 1.18.38 |
| **MapStruct** | Bean 转换 | 1.6.3 |
| **JUnit 5** | 单元测试 | 5.8.2 |
| **Mockito** | Mock 框架 | 4.8.0 |

### 任务与消息

| 技术 | 说明 | 版本 |
|------|------|------|
| **Quartz** | 任务调度 | 2.3.2 |
| **Flowable** | 工作流引擎 | 6.8.0 |
| **RocketMQ** | 消息队列 | - |
| **Kafka** | 消息队列 | - |
| **RabbitMQ** | 消息队列 | - |

### 监控与文档

| 技术 | 说明 | 版本 |
|------|------|------|
| **Springdoc** | API 文档 | 1.7.0 |
| **Knife4j** | API 界面 | 4.0.0+ |
| **Spring Boot Admin** | 监控平台 | 2.7.10 |
| **SkyWalking** | 链路追踪 | 8.12.0 |

### 基础设施

| 技术 | 说明 | 版本 |
|------|------|------|
| **Jackson** | JSON 处理 | 2.13.5 |
| **aj-captcha** | 验证码 | Latest |
| **Easy-Trans** | 数据翻译 | - |

## 项目差异对比 (Edition Comparison)

### 完整版 vs 精简版

| 特性 | 完整版 (ruoyi-vue-pro) | 精简版 (yudao-boot-mini-zt) |
|------|------------------------|--------------------------|
| **模块数量** | 20+ 模块 | 3 模块（系统、基础设施、数据工作室） |
| **编译速度** | 较慢 | 快速 |
| **功能范围** | 全业务场景 | 核心系统功能 |
| **适用场景** | 大型企业、复杂业务 | 快速原型、中小项目 |
| **代码行数** | 100k+ | 精简 |

### 迁移指南

如需从精简版迁移到完整版，参考[《迁移文档》](https://doc.iocoder.cn/migrate-module/)：
1. 启用所需模块（在 `pom.xml` 中取消注释）
2. 导入对应数据库脚本
3. 配置相关依赖和参数
4. 预计迁移时间：5-10 分钟

## 许可证 (License)

本项目使用 [MIT License](https://opensource.org/licenses/MIT) 开源协议，个人与企业可 100% 免费使用，无需保留类作者、Copyright 信息。

**比 Apache 2.0 更宽松的许可证，可以商用、私有化、二次开发、销售等。**

## 贡献指南 (Contributing)

欢迎提交 Issue 和 Pull Request！

### 提交规范
- 遵循《阿里巴巴 Java 开发手册》
- 代码注释率不低于 50%
- 添加必要的单元测试
- 更新相关文档

### 开发规范
1. **代码风格**: 遵循 Google Java Style Guide
2. **提交信息**: 使用中文，描述清晰
3. **测试覆盖**: 新增功能必须包含测试
4. **文档更新**: 重要变更需更新文档

## 社区与支持 (Community & Support)

### 官方渠道
- **文档站点**: <https://doc.iocoder.cn>
- **演示地址**: <http://dashboard-vue3.yudao.iocoder.cn>
- **GitHub**: <https://github.com/YunaiV/ruoyi-vue-pro>
- **Gitee**: <https://gitee.com/yudaocode/yudao-boot-mini>

### 交流群
- **问题反馈**: GitHub Issues
- **技术交流**: Gitee Discussions
- **微信联系**: Ai x9975（项目外包咨询）

### 致谢
感谢所有为 Yudao 项目做出贡献的开发者！

---

**声明**: 本项目坚持开源，现在、未来都不会有商业版本，所有代码全部开源！

**给项目点点 Star 吧，这对我们真的很重要！** ⭐️
