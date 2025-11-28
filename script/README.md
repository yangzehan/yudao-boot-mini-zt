# Flink微服务集群启动脚本说明

## 脚本文件

### 1. start-flink-services.bat
启动所有Flink微服务组件。

**启动顺序:**
1. Nacos Server (服务注册与发现)
2. Gateway (API网关)
3. Flink V1 Service (Flink 1.17.2服务)
4. Flink V2 Service (Flink 1.18.1服务)
5. Flink Admin Service (统一管理服务)

**访问地址:**
- Nacos Console: http://localhost:8848/nacos
- API Gateway:   http://localhost:8888
- Flink V1:      http://localhost:8084
- Flink V2:      http://localhost:8085
- Flink Admin:   http://localhost:8086

### 2. stop-flink-services.bat
停止所有相关服务进程。

## 使用方法

### Windows系统
```bash
# 启动服务
双击运行 start-flink-services.bat

# 停止服务
双击运行 stop-flink-services.bat
```

### Linux/Mac系统
```bash
# 启动服务
bash start-flink-services.sh

# 停止服务
bash stop-flink-services.sh
```

## 前置条件

1. **Java 8或更高版本**
   ```bash
   java -version
   ```

2. **Maven Daemon (推荐) 或 Maven**
   ```bash
   # 推荐使用mvnd，速度更快
   where mvnd  # Windows
   which mvnd  # Linux/Mac

   # 或使用标准mvn
   mvn -version
   ```

3. **Flink集群**
   - 确保Flink集群已启动
   - 默认端口: 8081 (Web UI)
   - 历史服务器: 8082

## 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 服务注册中心、控制台 |
| Gateway | 8888 | API网关 |
| Flink V1 | 8084 | Flink 1.17.2服务 |
| Flink V2 | 8085 | Flink 1.18.1服务 |
| Flink Admin | 8086 | 统一管理服务 |

## API调用示例

### 通过Gateway访问Flink V1服务
```bash
# 提交作业
curl -X POST http://localhost:8888/flink/v1/job/submit \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "test-job-v1",
    "mainClass": "com.example.JobMain",
    "parallelism": 2
  }'

# 查询作业状态
curl http://localhost:8888/flink/v1/job/{jobId}/status
```

### 通过Gateway访问Flink V2服务
```bash
# 提交作业
curl -X POST http://localhost:8888/flink/v2/job/submit \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "test-job-v2",
    "mainClass": "com.example.JobMain",
    "parallelism": 2
  }'
```

### 访问统一管理服务
```bash
# 获取所有版本信息
curl http://localhost:8888/flink/admin/versions

# 获取所有作业
curl http://localhost:8888/flink/admin/jobs

# 健康检查
curl http://localhost:8888/flink/admin/health
```

## 注意事项

1. **服务依赖顺序**
   - 请按照脚本中的顺序启动服务
   - Nacos Server必须首先启动

2. **端口占用**
   - 确保上述端口未被其他程序占用
   - 可通过 `netstat -ano | findstr :端口号` 查看端口占用情况

3. **Flink集群**
   - 请先启动Flink集群
   - 修改各服务的application-local.yaml配置文件中的Flink集群地址

4. **日志查看**
   - 所有服务日志会输出到控制台
   - 可在启动的单独窗口中查看详细日志

5. **停止服务**
   - 使用stop-flink-services.bat停止所有服务
   - 或在各个控制台窗口按Ctrl+C

## 故障排除

### 1. 端口被占用
```bash
# 查看端口占用
netstat -ano | findstr :8084

# 终止进程
taskkill /PID <进程ID> /F
```

### 2. Maven构建失败
```bash
# 清理并重新安装
mvnd clean install -DskipTests
```

### 3. 服务注册失败
- 检查Nacos是否正常启动
- 检查网络连接
- 查看服务日志中的错误信息

### 4. Flink集群连接失败
- 确认Flink集群已启动
- 检查application-local.yaml中的Flink集群地址配置
- 确认防火墙设置

## 扩展说明

### 添加新的Flink版本
1. 创建新的服务模块 (如: yudao-flink-v3-service)
2. 在pom.xml中添加模块声明
3. 在Gateway中配置路由规则
4. 更新启动脚本

### 集群部署
- 修改application.yaml中的Nacos地址为集群地址
- 配置多实例部署
- 使用负载均衡
