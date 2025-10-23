# OAuth2.0授权

<cite>
**本文档引用文件**  
- [OAuth2AccessTokenDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2AccessTokenDO.java)
- [OAuth2ClientDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ClientDO.java)
- [OAuth2ApproveDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ApproveDO.java)
- [OAuth2GrantTypeEnum.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/enums/oauth2/OAuth2GrantTypeEnum.java)
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)
- [OAuth2TokenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2TokenController.java)
- [OAuth2ClientController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2ClientController.java)
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构概述](#架构概述)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)

## 简介
本文档全面介绍基于 `yudao-boot-mini-zt` 项目的 OAuth2.0 授权服务器实现。涵盖四种授权模式（授权码、密码、客户端凭证、刷新令牌）的流程与接口规范，详细说明 OAuth2.0 数据模型设计、客户端管理机制、授权同意流程、第三方应用集成及安全最佳实践。

## 项目结构
项目采用模块化设计，OAuth2.0 相关功能主要集中在 `yudao-module-system` 模块中，涉及数据对象、服务实现、控制器和接口定义。

```mermaid
graph TB
subgraph "yudao-module-system"
subgraph "Controller"
OAuth2ClientController
OAuth2TokenController
OAuth2OpenController
end
subgraph "Service"
OAuth2ClientService
OAuth2ApproveService
OAuth2GrantService
OAuth2TokenService
end
subgraph "Data Object"
OAuth2ClientDO
OAuth2AccessTokenDO
OAuth2ApproveDO
end
subgraph "Enum"
OAuth2GrantTypeEnum
UserTypeEnum
end
OAuth2ClientController --> OAuth2ClientService
OAuth2TokenController --> OAuth2GrantService
OAuth2OpenController --> OAuth2ApproveService
OAuth2GrantService --> OAuth2AccessTokenDO
OAuth2ApproveService --> OAuth2ApproveDO
end
```

**图示来源**  
- [OAuth2ClientController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2ClientController.java)
- [OAuth2TokenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2TokenController.java)
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2AccessTokenDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2AccessTokenDO.java)
- [OAuth2ApproveDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ApproveDO.java)

**本节来源**  
- [项目结构](file://README.md)

## 核心组件
核心组件包括 OAuth2.0 客户端管理、令牌生成与验证、授权批准机制和四种标准授权模式的实现。

**本节来源**  
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)

## 架构概述
系统采用典型的 OAuth2.0 服务端架构，包含客户端注册、授权服务器、资源服务器三大核心部分。通过 Spring Security OAuth2 扩展实现，结合 MyBatis 和 Redis 实现持久化与缓存。

```mermaid
graph LR
Client[第三方应用] --> |请求授权| AuthServer[授权服务器]
AuthServer --> |验证客户端| ClientDB[(客户端数据库)]
AuthServer --> |生成令牌| TokenService[令牌服务]
TokenService --> Redis[(Redis缓存)]
AuthServer --> |用户确认| User[用户]
User --> |同意授权| AuthServer
Client --> |携带令牌访问| ResourceServer[资源服务器]
ResourceServer --> |校验令牌| TokenService
```

**图示来源**  
- [OAuth2TokenService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2TokenService.java)
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [RedisKeyConstants.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/redis/RedisKeyConstants.java)

## 详细组件分析

### 授权模式实现分析
系统完整实现了 OAuth2.0 四种授权模式：

```mermaid
classDiagram
class OAuth2GrantService {
+grantImplicit(userId, userType, clientId, scopes) OAuth2AccessTokenDO
+grantAuthorizationCode(userId, userType, clientId, scopes) String
+grantPassword(username, password, clientId) OAuth2AccessTokenDO
+grantClientCredentials(clientId) OAuth2AccessTokenDO
+grantRefreshToken(refreshToken, clientId) OAuth2AccessTokenDO
}
class OAuth2GrantTypeEnum {
+PASSWORD
+AUTHORIZATION_CODE
+IMPLICIT
+CLIENT_CREDENTIALS
+REFRESH_TOKEN
}
OAuth2GrantService --> OAuth2GrantTypeEnum : "支持"
```

**图示来源**  
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)
- [OAuth2GrantTypeEnum.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/enums/oauth2/OAuth2GrantTypeEnum.java)

#### 授权码模式流程
```mermaid
sequenceDiagram
participant Client as "客户端应用"
participant AuthController as "OAuth2OpenController"
participant ApproveService as "OAuth2ApproveService"
participant GrantService as "OAuth2GrantService"
Client->>AuthController : GET /oauth2/authorize
AuthController->>ApproveService : checkForPreApproval()
ApproveService-->>AuthController : 返回预批准结果
alt 已预批准
AuthController->>GrantService : grantAuthorizationCode()
GrantService-->>AuthController : 返回授权码
AuthController-->>Client : 重定向带回code
else 需要用户确认
AuthController-->>Client : 返回授权页面
Client->>AuthController : POST /oauth2/authorize/approve
AuthController->>ApproveService : updateAfterApproval()
ApproveService-->>AuthController : 更新批准记录
AuthController->>GrantService : grantAuthorizationCode()
GrantService-->>AuthController : 返回授权码
AuthController-->>Client : 重定向带回code
end
```

**图示来源**  
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)

### OAuth2AccessTokenDO 数据模型
`OAuth2AccessTokenDO` 数据对象定义了访问令牌的核心属性和存储结构。

```mermaid
erDiagram
OAUTH2_ACCESS_TOKEN {
bigint id PK
varchar accessToken UK
varchar refreshToken
bigint userId
int userType
varchar clientId FK
json userInfo
json scopes
timestamp expiresTime
timestamp createTime
}
OAUTH2_CLIENT {
varchar id PK
varchar name
varchar clientSecret
json redirectUris
json authorizedGrantTypes
json scopes
json autoApproveScopes
int accessTokenValiditySeconds
int refreshTokenValiditySeconds
}
OAUTH2_APPROVE {
bigint id PK
bigint userId
int userType
varchar clientId FK
varchar scope
boolean approved
timestamp expiresTime
}
OAUTH2_ACCESS_TOKEN ||--o{ OAUTH2_CLIENT : "clientId"
OAUTH2_APPROVE ||--o{ OAUTH2_CLIENT : "clientId"
```

**图示来源**  
- [OAuth2AccessTokenDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2AccessTokenDO.java)
- [OAuth2ClientDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ClientDO.java)
- [OAuth2ApproveDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ApproveDO.java)

**本节来源**  
- [OAuth2AccessTokenDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2AccessTokenDO.java)
- [OAuth2ClientDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ClientDO.java)
- [OAuth2ApproveDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/oauth2/OAuth2ApproveDO.java)

### 客户端管理机制
客户端管理通过 `OAuth2ClientService` 实现，提供完整的 CRUD 操作和缓存机制。

```mermaid
flowchart TD
Start([客户端管理入口]) --> List["获取客户端列表"]
List --> Filter["按名称/状态过滤"]
Start --> Create["创建客户端"]
Create --> Validate["验证参数"]
Validate --> Store["存储到数据库"]
Store --> Cache["更新缓存"]
Start --> Update["更新客户端"]
Update --> Find["查找现有记录"]
Find --> Modify["修改属性"]
Modify --> Store
Start --> Delete["删除客户端"]
Delete --> Remove["从数据库移除"]
Remove --> Invalidate["失效缓存"]
Invalidate --> End([操作完成])
```

**图示来源**  
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [OAuth2ClientServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientServiceImpl.java)
- [OAuth2ClientMapper.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/mysql/oauth2/OAuth2ClientMapper.java)

**本节来源**  
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)
- [OAuth2ClientServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientServiceImpl.java)

### 授权同意机制
授权同意机制通过 `OAuth2ApproveService` 实现，记录用户对客户端的授权决策。

```mermaid
sequenceDiagram
participant User as "用户"
participant Controller as "OAuth2OpenController"
participant ApproveService as "OAuth2ApproveService"
participant Client as "客户端应用"
User->>Controller : 访问授权页面
Controller->>ApproveService : checkForPreApproval()
ApproveService->>Client : 检查自动批准范围
alt 全部自动批准
ApproveService-->>Controller : 返回已批准
Controller->>User : 直接跳转完成
else 部分需确认
ApproveService-->>Controller : 返回待确认范围
Controller-->>User : 显示授权确认页面
User->>Controller : 提交授权选择
Controller->>ApproveService : updateAfterApproval()
ApproveService-->>Controller : 保存批准记录
Controller-->>User : 授权完成
end
```

**图示来源**  
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2ApproveServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveServiceImpl.java)
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)

**本节来源**  
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2ApproveServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveServiceImpl.java)

## 依赖分析
系统依赖关系清晰，各组件职责分明。

```mermaid
graph TD
OAuth2ClientController --> OAuth2ClientService
OAuth2TokenController --> OAuth2GrantService
OAuth2OpenController --> OAuth2ApproveService
OAuth2OpenController --> OAuth2GrantService
OAuth2GrantService --> OAuth2ClientService
OAuth2GrantService --> OAuth2ApproveService
OAuth2GrantService --> OAuth2AccessTokenDO
OAuth2ApproveService --> OAuth2ClientService
OAuth2ApproveService --> OAuth2ApproveDO
OAuth2ClientService --> OAuth2ClientDO
OAuth2ClientService --> Redis
```

**图示来源**  
- [OAuth2ClientController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2ClientController.java)
- [OAuth2TokenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2TokenController.java)
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)
- [OAuth2GrantService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantService.java)
- [OAuth2ApproveService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveService.java)
- [OAuth2ClientService.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientService.java)

**本节来源**  
- [OAuth2ClientController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2ClientController.java)
- [OAuth2TokenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2TokenController.java)
- [OAuth2OpenController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/oauth2/OAuth2OpenController.java)

## 性能考虑
系统通过多级缓存和数据库优化确保高性能：
- 客户端信息缓存于 Redis，减少数据库查询
- 令牌验证采用内存缓存机制
- 数据库表建立适当索引（clientId, userId等）
- 批量操作支持提高管理效率

## 故障排除指南
常见问题及解决方案：
- **授权码失效**：检查授权码有效期配置
- **客户端验证失败**：确认 clientId 和 clientSecret 正确性
- **重定向 URI 不匹配**：检查客户端配置的 redirectUris
- **授权范围不足**：确认请求的 scopes 在客户端允许范围内
- **令牌过期**：使用刷新令牌机制获取新令牌

**本节来源**  
- [OAuth2ClientServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ClientServiceImpl.java)
- [OAuth2GrantServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2GrantServiceImpl.java)
- [OAuth2ApproveServiceImpl.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2ApproveServiceImpl.java)

## 结论
本系统实现了完整的 OAuth2.0 授权服务器功能，支持四种标准授权模式，具备完善的客户端管理、授权同意、令牌管理和安全控制机制。通过合理的架构设计和性能优化，能够满足企业级应用的安全授权需求。