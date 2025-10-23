# API参考

<cite>
**本文档引用的文件**
- [UserController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/UserController.java)
- [DeptController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/dept/DeptController.java)
- [RoleController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/permission/RoleController.java)
- [TenantController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/tenant/TenantController.java)
- [AuthController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/AuthController.java)
- [WebProperties.java](file://yudao-framework/yudao-spring-boot-starter-web/src/main/java/cn/iocoder/yudao/framework/web/config/WebProperties.java)
- [AdminUserApi.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/api/user/AdminUserApi.java)
- [AdminUserDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/user/AdminUserDO.java)
</cite>

## 目录
1. [简介](#简介)
2. [API基础信息](#api基础信息)
3. [用户管理API](#用户管理api)
4. [部门管理API](#部门管理api)
5. [角色管理API](#角色管理api)
6. [租户管理API](#租户管理api)
7. [认证授权API](#认证授权api)
8. [认证与授权机制](#认证与授权机制)
9. [API版本管理](#api版本管理)
10. [请求响应示例](#请求响应示例)
11. [限流与熔断策略](#限流与熔断策略)
12. [API文档工具](#api文档工具)
13. [客户端调用指南](#客户端调用指南)

## 简介
本API参考文档系统性地记录了项目提供的所有RESTful API接口，重点针对UserController、RoleController、DeptController、TenantController等核心控制器。文档详细描述了每个API端点的HTTP方法、URL路径、请求参数、请求体结构、响应格式和状态码，为客户端开发者提供了完整的调用指南和最佳实践。

## API基础信息

### 基础URL
所有API的基础URL为`/system`，根据WebProperties配置，API前缀为`/admin-api`，因此完整的API访问路径为`/admin-api/system`。

### 请求响应格式
所有API遵循统一的响应格式，使用CommonResult封装返回结果：
```json
{
  "code": 0,
  "msg": "成功",
  "data": {}
}
```

### 通用状态码
- `0`: 成功
- `非0`: 失败，具体错误码参考GlobalErrorCodeConstants

**Section sources**
- [WebProperties.java](file://yudao-framework/yudao-spring-boot-starter-web/src/main/java/cn/iocoder/yudao/framework/web/config/WebProperties.java#L1-L67)
- [CommonResult.java](file://yudao-framework/yudao-common/src/main/java/cn/iocoder/yudao/framework/common/pojo/CommonResult.java)

## 用户管理API

### 创建用户
**HTTP方法**: POST  
**URL路径**: `/system/user/create`  
**权限要求**: `system:user:create`  
**请求体**: UserSaveReqVO对象，包含用户名、密码、邮箱、手机号等用户信息  
**响应**: 创建成功的用户ID  
**描述**: 新增系统用户，需要提供完整的用户信息。

### 修改用户
**HTTP方法**: PUT  
**URL路径**: `/system/user/update`  
**权限要求**: `system:user:update`  
**请求体**: UserSaveReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 更新现有用户信息。

### 删除用户
**HTTP方法**: DELETE  
**URL路径**: `/system/user/delete`  
**权限要求**: `system:user:delete`  
**请求参数**: id (用户编号)  
**响应**: 操作结果布尔值  
**描述**: 根据用户ID删除单个用户。

### 批量删除用户
**HTTP方法**: DELETE  
**URL路径**: `/system/user/delete-list`  
**权限要求**: `system:user:delete`  
**请求参数**: ids (用户编号列表)  
**响应**: 操作结果布尔值  
**描述**: 批量删除多个用户。

### 重置用户密码
**HTTP方法**: PUT  
**URL路径**: `/system/user/update-password`  
**权限要求**: `system:user:update-password`  
**请求体**: UserUpdatePasswordReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 重置指定用户的密码。

### 修改用户状态
**HTTP方法**: PUT  
**URL路径**: `/system/user/update-status`  
**权限要求**: `system:user:update`  
**请求体**: UserUpdateStatusReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 修改用户启用/禁用状态。

### 获取用户分页列表
**HTTP方法**: GET  
**URL路径**: `/system/user/page`  
**权限要求**: `system:user:query`  
**请求参数**: UserPageReqVO对象  
**响应**: PageResult<UserRespVO>分页结果  
**描述**: 分页查询用户列表，包含部门信息。

### 获取用户精简信息列表
**HTTP方法**: GET  
**URL路径**: `/system/user/list-all-simple` 或 `/system/user/simple-list`  
**权限要求**: `system:user:query`  
**响应**: List<UserSimpleRespVO>  
**描述**: 获取启用状态的用户精简信息列表，主要用于前端下拉选项。

### 获取用户详情
**HTTP方法**: GET  
**URL路径**: `/system/user/get`  
**权限要求**: `system:user:query`  
**请求参数**: id (用户编号)  
**响应**: UserRespVO对象  
**描述**: 根据用户ID获取用户详细信息。

### 导出用户
**HTTP方法**: GET  
**URL路径**: `/system/user/export-excel`  
**权限要求**: `system:user:export`  
**请求参数**: UserPageReqVO对象  
**响应**: Excel文件  
**描述**: 导出用户数据到Excel文件。

### 获取导入用户模板
**HTTP方法**: GET  
**URL路径**: `/system/user/get-import-template`  
**响应**: Excel文件  
**描述**: 获取用户导入模板文件。

### 导入用户
**HTTP方法**: POST  
**URL路径**: `/system/user/import`  
**权限要求**: `system:user:import`  
**请求参数**: 
- file: Excel文件
- updateSupport: 是否支持更新，默认false  
**响应**: UserImportRespVO对象  
**描述**: 从Excel文件批量导入用户数据。

**Section sources**
- [UserController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/UserController.java#L1-L181)
- [UserSaveReqVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/vo/user/UserSaveReqVO.java)
- [UserRespVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/vo/user/UserRespVO.java)
- [AdminUserDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/user/AdminUserDO.java)

## 部门管理API

### 创建部门
**HTTP方法**: POST  
**URL路径**: `/system/dept/create`  
**权限要求**: `system:dept:create`  
**请求体**: DeptSaveReqVO对象  
**响应**: 创建成功的部门ID  
**描述**: 新增部门信息。

### 更新部门
**HTTP方法**: PUT  
**URL路径**: `/system/dept/update`  
**权限要求**: `system:dept:update`  
**请求体**: DeptSaveReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 更新现有部门信息。

### 删除部门
**HTTP方法**: DELETE  
**URL路径**: `/system/dept/delete`  
**权限要求**: `system:dept:delete`  
**请求参数**: id (部门编号)  
**响应**: 操作结果布尔值  
**描述**: 根据部门ID删除部门。

### 批量删除部门
**HTTP方法**: DELETE  
**URL路径**: `/system/dept/delete-list`  
**权限要求**: `system:dept:delete`  
**请求参数**: ids (部门编号列表)  
**响应**: 操作结果布尔值  
**描述**: 批量删除多个部门。

### 获取部门列表
**HTTP方法**: GET  
**URL路径**: `/system/dept/list`  
**权限要求**: `system:dept:query`  
**请求参数**: DeptListReqVO对象  
**响应**: List<DeptRespVO>  
**描述**: 查询部门列表。

### 获取部门精简信息列表
**HTTP方法**: GET  
**URL路径**: `/system/dept/list-all-simple` 或 `/system/dept/simple-list`  
**权限要求**: `system:dept:query`  
**响应**: List<DeptSimpleRespVO>  
**描述**: 获取启用状态的部门精简信息列表，主要用于前端下拉选项。

### 获取部门详情
**HTTP方法**: GET  
**URL路径**: `/system/dept/get`  
**权限要求**: `system:dept:query`  
**请求参数**: id (部门编号)  
**响应**: DeptRespVO对象  
**描述**: 根据部门ID获取部门详细信息。

**Section sources**
- [DeptController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/dept/DeptController.java#L1-L94)
- [DeptSaveReqVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/dept/vo/dept/DeptSaveReqVO.java)
- [DeptRespVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/dept/vo/dept/DeptRespVO.java)
- [DeptDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/dept/DeptDO.java)

## 角色管理API

### 创建角色
**HTTP方法**: POST  
**URL路径**: `/system/role/create`  
**权限要求**: `system:role:create`  
**请求体**: RoleSaveReqVO对象  
**响应**: 创建成功的角色ID  
**描述**: 新增角色信息。

### 修改角色
**HTTP方法**: PUT  
**URL路径**: `/system/role/update`  
**权限要求**: `system:role:update`  
**请求体**: RoleSaveReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 更新现有角色信息。

### 删除角色
**HTTP方法**: DELETE  
**URL路径**: `/system/role/delete`  
**权限要求**: `system:role:delete`  
**请求参数**: id (角色编号)  
**响应**: 操作结果布尔值  
**描述**: 根据角色ID删除角色。

### 批量删除角色
**HTTP方法**: DELETE  
**URL路径**: `/system/role/delete-list`  
**权限要求**: `system:role:delete`  
**请求参数**: ids (角色编号列表)  
**响应**: 操作结果布尔值  
**描述**: 批量删除多个角色。

### 获取角色详情
**HTTP方法**: GET  
**URL路径**: `/system/role/get`  
**权限要求**: `system:role:query`  
**请求参数**: id (角色编号)  
**响应**: RoleRespVO对象  
**描述**: 根据角色ID获取角色详细信息。

### 获取角色分页列表
**HTTP方法**: GET  
**URL路径**: `/system/role/page`  
**权限要求**: `system:role:query`  
**请求参数**: RolePageReqVO对象  
**响应**: PageResult<RoleRespVO>分页结果  
**描述**: 分页查询角色列表。

### 获取角色精简信息列表
**HTTP方法**: GET  
**URL路径**: `/system/role/list-all-simple` 或 `/system/role/simple-list`  
**权限要求**: `system:role:query`  
**响应**: List<RoleRespVO>  
**描述**: 获取启用状态的角色精简信息列表，主要用于前端下拉选项。

### 导出角色Excel
**HTTP方法**: GET  
**URL路径**: `/system/role/export-excel`  
**权限要求**: `system:role:export`  
**请求参数**: RolePageReqVO对象  
**响应**: Excel文件  
**描述**: 导出角色数据到Excel文件。

**Section sources**
- [RoleController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/permission/RoleController.java#L1-L111)
- [RoleSaveReqVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/permission/vo/role/RoleSaveReqVO.java)
- [RoleRespVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/permission/vo/role/RoleRespVO.java)
- [RoleDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/permission/RoleDO.java)

## 租户管理API

### 获取租户ID
**HTTP方法**: GET  
**URL路径**: `/system/tenant/get-id-by-name`  
**权限要求**: 无需登录  
**请求参数**: name (租户名)  
**响应**: 租户ID  
**描述**: 根据租户名获取租户ID，用于登录界面。

### 获取租户精简列表
**HTTP方法**: GET  
**URL路径**: `/system/tenant/simple-list`  
**权限要求**: 无需登录  
**响应**: List<TenantRespVO>  
**描述**: 获取启用状态的租户精简信息列表。

### 获取租户信息
**HTTP方法**: GET  
**URL路径**: `/system/tenant/get-by-website`  
**权限要求**: 无需登录  
**请求参数**: website (域名)  
**响应**: TenantRespVO对象  
**描述**: 根据域名获取租户信息。

### 创建租户
**HTTP方法**: POST  
**URL路径**: `/system/tenant/create`  
**权限要求**: `system:tenant:create`  
**请求体**: TenantSaveReqVO对象  
**响应**: 创建成功的租户ID  
**描述**: 新增租户信息。

### 更新租户
**HTTP方法**: PUT  
**URL路径**: `/system/tenant/update`  
**权限要求**: `system:tenant:update`  
**请求体**: TenantSaveReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 更新现有租户信息。

### 删除租户
**HTTP方法**: DELETE  
**URL路径**: `/system/tenant/delete`  
**权限要求**: `system:tenant:delete`  
**请求参数**: id (租户编号)  
**响应**: 操作结果布尔值  
**描述**: 根据租户ID删除租户。

### 批量删除租户
**HTTP方法**: DELETE  
**URL路径**: `/system/tenant/delete-list`  
**权限要求**: `system:tenant:delete`  
**请求参数**: ids (租户编号列表)  
**响应**: 操作结果布尔值  
**描述**: 批量删除多个租户。

### 获取租户详情
**HTTP方法**: GET  
**URL路径**: `/system/tenant/get`  
**权限要求**: `system:tenant:query`  
**请求参数**: id (租户编号)  
**响应**: TenantRespVO对象  
**描述**: 根据租户ID获取租户详细信息。

### 获取租户分页列表
**HTTP方法**: GET  
**URL路径**: `/system/tenant/page`  
**权限要求**: `system:tenant:query`  
**请求参数**: TenantPageReqVO对象  
**响应**: PageResult<TenantRespVO>分页结果  
**描述**: 分页查询租户列表。

### 导出租户Excel
**HTTP方法**: GET  
**URL路径**: `/system/tenant/export-excel`  
**权限要求**: `system:tenant:export`  
**请求参数**: TenantPageReqVO对象  
**响应**: Excel文件  
**描述**: 导出租户数据到Excel文件。

**Section sources**
- [TenantController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/tenant/TenantController.java#L1-L136)
- [TenantSaveReqVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/tenant/vo/tenant/TenantSaveReqVO.java)
- [TenantRespVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/tenant/vo/tenant/TenantRespVO.java)
- [TenantDO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/tenant/TenantDO.java)

## 认证授权API

### 账号密码登录
**HTTP方法**: POST  
**URL路径**: `/system/auth/login`  
**权限要求**: 无需登录  
**请求体**: AuthLoginReqVO对象  
**响应**: AuthLoginRespVO对象，包含访问令牌和刷新令牌  
**描述**: 使用账号密码进行系统登录。

### 登出系统
**HTTP方法**: POST  
**URL路径**: `/system/auth/logout`  
**权限要求**: 无需登录  
**响应**: 操作结果布尔值  
**描述**: 退出登录，清除令牌。

### 刷新令牌
**HTTP方法**: POST  
**URL路径**: `/system/auth/refresh-token`  
**权限要求**: 无需登录  
**请求参数**: refreshToken (刷新令牌)  
**响应**: AuthLoginRespVO对象  
**描述**: 使用刷新令牌获取新的访问令牌。

### 获取权限信息
**HTTP方法**: GET  
**URL路径**: `/system/auth/get-permission-info`  
**权限要求**: 已登录  
**响应**: AuthPermissionInfoRespVO对象  
**描述**: 获取当前登录用户的权限信息，包括用户信息、角色列表和菜单列表。

### 注册用户
**HTTP方法**: POST  
**URL路径**: `/system/auth/register`  
**权限要求**: 无需登录  
**请求体**: AuthRegisterReqVO对象  
**响应**: AuthLoginRespVO对象  
**描述**: 新用户注册。

### 短信验证码登录
**HTTP方法**: POST  
**URL路径**: `/system/auth/sms-login`  
**权限要求**: 无需登录  
**请求体**: AuthSmsLoginReqVO对象  
**响应**: AuthLoginRespVO对象  
**描述**: 使用手机号和短信验证码登录。

### 发送短信验证码
**HTTP方法**: POST  
**URL路径**: `/system/auth/send-sms-code`  
**权限要求**: 无需登录  
**请求体**: AuthSmsSendReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 发送手机验证码。

### 重置密码
**HTTP方法**: POST  
**URL路径**: `/system/auth/reset-password`  
**权限要求**: 无需登录  
**请求体**: AuthResetPasswordReqVO对象  
**响应**: 操作结果布尔值  
**描述**: 通过手机号和验证码重置密码。

### 社交授权跳转
**HTTP方法**: GET  
**URL路径**: `/system/auth/social-auth-redirect`  
**权限要求**: 无需登录  
**请求参数**: 
- type: 社交类型
- redirectUri: 回调路径  
**响应**: 授权URL字符串  
**描述**: 获取社交登录的授权跳转URL。

### 社交快捷登录
**HTTP方法**: POST  
**URL路径**: `/system/auth/social-login`  
**权限要求**: 无需登录  
**请求体**: AuthSocialLoginReqVO对象  
**响应**: AuthLoginRespVO对象  
**描述**: 使用社交账号的授权码进行快捷登录。

**Section sources**
- [AuthController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/AuthController.java#L1-L174)
- [AuthLoginReqVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/vo/AuthLoginReqVO.java)
- [AuthLoginRespVO.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/vo/AuthLoginRespVO.java)

## 认证与授权机制

### JWT令牌使用
系统采用JWT（JSON Web Token）作为认证机制。登录成功后，服务器返回包含用户信息的JWT令牌，客户端在后续请求中需要在Authorization头中携带该令牌。

### 权限校验规则
系统使用Spring Security进行权限控制，通过@PreAuthorize注解实现方法级别的权限校验。权限校验基于用户的角色和权限分配，使用`@ss.hasPermission('permission:code')`表达式进行判断。

### 认证流程
1. 用户通过账号密码或短信验证码等方式登录
2. 服务器验证凭证，生成JWT令牌
3. 客户端存储令牌并在后续请求中携带
4. 服务器验证令牌有效性，获取用户身份
5. 执行权限校验，决定是否允许访问

### 令牌刷新机制
系统提供刷新令牌机制，当访问令牌过期时，客户端可以使用刷新令牌获取新的访问令牌，避免用户频繁重新登录。

**Section sources**
- [AuthController.java](file://yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/AuthController.java#L1-L174)
- [SecurityProperties.java](file://yudao-framework/yudao-spring-boot-starter-security/src/main/java/cn/iocoder/yudao/framework/security/config/SecurityProperties.java)

## API版本管理
系统目前采用单一版本管理策略，所有API位于`/admin-api/system`路径下。未来如需支持多版本，将通过在URL路径中添加版本号的方式实现，例如`/admin-api/v1/system`。系统会确保向后兼容性，旧版本API在废弃前会保持可用。

## 请求响应示例

### 成功响应示例
```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "id": 1024,
    "username": "admin",
    "nickname": "管理员"
  }
}
```

### 错误响应示例
```json
{
  "code": 10001,
  "msg": "用户不存在",
  "data": null
}
```

### 用户创建请求示例
```json
{
  "username": "newuser",
  "password": "123456",
  "email": "newuser@example.com",
  "mobile": "13800138000"
}
```

## 限流与熔断策略
系统通过yudao-spring-boot-starter-protection模块提供限流和熔断保护。对于关键API如登录、短信发送等，设置了基于Redis的限流策略，防止恶意请求和系统过载。熔断机制通过Hystrix或Resilience4j实现，当服务出现异常时自动熔断，保护系统稳定性。

## API文档工具
系统集成了Knife4j（Swagger增强版）作为API文档生成工具。开发者可以通过访问`/doc.html`路径查看交互式API文档。文档自动生成，包含所有API的详细信息、请求示例和在线测试功能。

**Section sources**
- [yudao-spring-boot-starter-web](file://yudao-framework/yudao-spring-boot-starter-web)

## 客户端调用指南

### 基本调用流程
1. 引入必要的依赖库
2. 配置API基础URL
3. 实现认证逻辑（登录获取令牌）
4. 在请求头中添加认证信息
5. 调用具体API接口
6. 处理响应结果

### 最佳实践
- 妥善管理JWT令牌，设置合理的存储和刷新策略
- 实现错误重试机制，特别是网络不稳定的情况
- 使用连接池管理HTTP连接，提高性能
- 实现日志记录，便于问题排查
- 定期更新API客户端，保持与服务端兼容

### 错误处理
- 检查响应码，区分成功和失败情况
- 解析错误信息，提供用户友好的提示
- 对于认证失败，引导用户重新登录
- 对于限流错误，提示用户稍后重试