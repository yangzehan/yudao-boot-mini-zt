# 数据中台模块 (yudao-module-datastudio)

## 模块简介

数据中台模块是 Yudao 快速开发平台的数据中心模块，主要提供文件管理功能，包括文件夹和文件的创建、编辑、删除、移动等操作，支持树形结构的文件管理。该模块与前端 DataStudio 组件配合使用，为用户提供类似 VS Code 的代码编辑体验。

## 功能特性

- 📁 **文件树管理**: 支持文件夹和文件的层级管理
- 📝 **文件编辑**: 支持 SQL 文件和普通文件的在线编辑
- 🔍 **文件搜索**: 支持按文件名搜索文件
- 📂 **文件操作**: 支持创建、编辑、删除、重命名、移动文件
- 🌳 **树形结构**: 自动构建树形结构展示
- 🔒 **权限控制**: 基于 Spring Security 的权限控制

## 项目结构

```
yudao-module-datastudio
├── pom.xml                                          # Maven 配置文件
├── README.md                                        # 模块说明文档
└── src
    └── main
        └── java
            └── cn
                └── iocoder
                    └── yudao
                        └── module
                            └── datastudio
                                ├── controller
                                │   └── admin
                                │       └── file
                                │           ├── FileManageController.java        # 文件管理控制器
                                │           └── vo
                                │               ├── file
                                │               │   └── FileManageListReqVO.java   # 列表查询VO
                                │               ├── save
                                │               │   └── FileManageSaveReqVO.java   # 保存请求VO
                                │               └── resp
                                │                   └── FileManageRespVO.java     # 响应VO
                                ├── dal
                                │   ├── dataobject
                                │   │   └── file
                                │   │       └── FileManageDO.java                # 文件实体
                                │   └── mysql
                                │       └── file
                                │           └── FileManageMapper.java            # Mapper接口
                                └── service
                                    └── file
                                        ├── FileManageService.java               # Service接口
                                        └── FileManageServiceImpl.java           # Service实现
```

## 快速开始

### 1. 数据库初始化

执行数据库初始化脚本：

```sql
-- 在数据库中执行
source sql/create_table.sql;
```

这将创建 `datastudio_file_manage` 表并插入示例数据。

### 2. 在主项目中引入模块

在 `yudao-server/pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cn.iocoder.yudao</groupId>
    <artifactId>yudao-module-datastudio</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 配置数据库连接

在 `application.yaml` 中配置数据库连接：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yudao_cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: password

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: cn.iocoder.yudao.module.datastudio.dal.dataobject
```

### 4. 启动应用

启动 `YudaoServerApplication` 即可。

## API 接口文档

### 文件管理接口

#### 1. 创建文件/文件夹

```http
POST /datastudio/file/create
Content-Type: application/json

{
  "name": "我的文件.sql",
  "type": "sql",
  "parentId": 1,
  "filePath": "/project/my-file.sql",
  "sort": 0,
  "status": 1
}
```

#### 2. 更新文件

```http
PUT /datastudio/file/update
Content-Type: application/json

{
  "id": 1,
  "name": "新的文件名.sql",
  "type": "sql",
  "parentId": 1,
  "filePath": "/project/new-name.sql"
}
```

#### 3. 删除文件

```http
DELETE /datastudio/file/delete?id=1
```

#### 4. 获取文件列表

```http
GET /datastudio/file/list?keyword=task&type=sql&parentId=1
```

#### 5. 获取文件树

```http
GET /datastudio/file/tree
```

响应示例：

```json
[
  {
    "id": 1,
    "name": "项目文件夹",
    "type": "folder",
    "parentId": 0,
    "filePath": "/project",
    "sort": 0,
    "fileSize": 0,
    "status": 1,
    "children": [
      {
        "id": 2,
        "name": "SQL 任务",
        "type": "folder",
        "parentId": 1,
        "filePath": "/project/sql",
        "sort": 1,
        "children": [
          {
            "id": 3,
            "name": "task1.sql",
            "type": "sql",
            "parentId": 2,
            "filePath": "/project/sql/task1.sql",
            "content": "-- task1.sql\n-- 文件内容编辑区域\nSELECT * FROM table_name;",
            "sort": 1,
            "fileSize": 1024,
            "status": 1
          }
        ]
      }
    ]
  }
]
```

#### 6. 获取子文件列表

```http
GET /datastudio/file/children?parentId=1
```

#### 7. 搜索文件

```http
GET /datastudio/file/search?keyword=task
```

#### 8. 保存文件内容

```http
POST /datastudio/file/save-content?id=1&content=SELECT * FROM users;
```

#### 9. 获取文件内容

```http
GET /datastudio/file/get-content?id=1
```

## 数据库表结构

### datastudio_file_manage

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 文件ID（主键） |
| name | varchar(100) | 文件/文件夹名称 |
| type | varchar(20) | 文件类型（folder/ sql/ file） |
| parent_id | bigint | 父文件夹ID |
| file_path | varchar(500) | 文件路径 |
| content | longtext | 文件内容 |
| sort | int | 显示顺序 |
| file_size | bigint | 文件大小（字节） |
| status | int | 状态（0-禁用，1-启用） |
| tenant_id | bigint | 租户编号 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

## 权限配置

### 菜单配置

在系统的菜单管理中添加以下菜单：

```
菜单名称: 数据中台
菜单路径: /datastudio
菜单图标: ep:folder
```

### 权限点配置

```
datastudio:file:create    - 创建文件
datastudio:file:update    - 更新文件
datastudio:file:delete    - 删除文件
datastudio:file:query     - 查询文件
```

### 角色配置

将权限点分配给相应的角色。

## 前端集成

### Vue 组件使用

在 Vue 前端项目中，可以使用以下方式集成：

```vue
<template>
  <div>
    <!-- 文件树 -->
    <el-tree
      :data="fileTree"
      @node-click="handleNodeClick"
      @node-contextmenu="handleContextMenu"
    >
      <template #default="{ node, data }">
        <span>
          <el-icon v-if="data.type === 'folder'"><FolderOpened /></el-icon>
          <el-icon v-else-if="data.type === 'sql'"><Document /></el-icon>
          <el-icon v-else><FileText /></el-icon>
          <span>{{ data.name }}</span>
        </span>
      </template>
    </el-tree>

    <!-- 编辑器 -->
    <MonacoEditor
      v-model="editorContent"
      language="sql"
      :height="'100%'"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFileTree, getFileContent, saveFileContent } from '@/api/datastudio/file'

const fileTree = ref([])
const editorContent = ref('')

// 获取文件树
const loadFileTree = async () => {
  const { data } = await getFileTree()
  fileTree.value = data
}

// 加载文件内容
const loadFileContent = async (fileId: number) => {
  const { data } = await getFileContent(fileId)
  editorContent.value = data
}

// 保存文件内容
const handleSave = async () => {
  // 保存逻辑
  await saveFileContent({ id: selectedFileId, content: editorContent.value })
}

onMounted(() => {
  loadFileTree()
})
</script>
```

### API 接口调用

在 `src/api/datastudio/file.ts` 中定义 API 调用：

```typescript
import request from '@/utils/request'

export const getFileTree = () => {
  return request({
    url: '/datastudio/file/tree',
    method: 'get'
  })
}

export const getFileContent = (id: number) => {
  return request({
    url: '/datastudio/file/get-content',
    method: 'get',
    params: { id }
  })
}

export const saveFileContent = (data: { id: number; content: string }) => {
  return request({
    url: '/datastudio/file/save-content',
    method: 'post',
    params: data
  })
}
```

## 注意事项

1. **权限控制**: 默认开启了 Spring Security 权限控制，需要在配置中开启相关注解
2. **事务支持**: 所有 Service 方法都支持事务，可以放心使用
3. **多租户**: 支持多租户，自动处理租户ID
4. **文件大小**: 文件内容存储在数据库中，适用于小文件，不适合大文件
5. **文件类型**: 目前支持 folder（文件夹）、sql（SQL文件）、file（普通文件）三种类型

## 扩展开发

### 添加新的文件类型

1. 在 `FileManageDO` 中添加新的类型常量
2. 在 Controller 中添加对应的处理逻辑
3. 在前端组件中添加对应的图标和操作

### 自定义文件存储

如果需要将文件存储在文件系统或云存储中，可以：

1. 修改 `FileManageDO` 中的 `content` 字段为文件路径
2. 添加文件上传/下载服务
3. 实现文件的读写逻辑

## 常见问题

### Q: 如何支持大文件？

A: 当前实现将文件内容存储在数据库中，适合小文件。对于大文件，建议使用云存储（如 OSS）或本地文件系统，并将文件路径存储在数据库中。

### Q: 如何实现文件版本控制？

A: 可以创建 `FileHistoryDO` 表，记录文件的历史版本，在保存文件时创建历史记录。

### Q: 如何添加文件上传功能？

A: 可以使用 Spring Boot 的文件上传功能，或集成 `apachecommons-fileupload`，将上传的文件内容保存到数据库。

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个模块。

## 许可证

本项目采用 Apache 2.0 许可证。
