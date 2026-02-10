# 实施计划：Flink History Server 监控功能集成到 yudao-flink-monitor

## 任务类型
- [x] Backend

## 目标
将 Flink History Server 监控功能集成到 **yudao-flink-monitor** 模块中，复用现有架构，不新增独立服务或 API 接口。

## 技术方案

### 方案：复用现有 Monitor 架构

**原因：**
1. History Server 与 JobManager 使用相同的 REST API
2. 现有 `MonitorConfig` 已配置 RestClient，可直接复用
3. `FlinkJobMonitor` 接口设计灵活，易于扩展
4. 不新增服务/接口，只在 monitor 模块中实现

### 核心组件

```
新增组件：
├── dto/HistoryJobInfo.java              # 历史作业信息 DTO
└── monitor/HistoryServerMonitor.java    # History Server 监控器（实现 FlinkJobMonitor）
```

复用组件：
- `MonitorConfig` - 已有 RestClient 配置
- `RestClient` - 已有 HTTP 客户端
- 现有 API 接口（通过 monitor 调用，不新增）

## 实施步骤

### Step 1: 添加历史作业 DTO 类

**文件：** `yudao-flink-common/src/main/java/cn/iocoder/yudao/module/flink/common/dto/HistoryJobInfo.java`

```java
@Data
public class HistoryJobInfo {
    /** 作业ID */
    private String jobId;
    /** 作业名称 */
    private String jobName;
    /** 作业状态: FINISHED, FAILED, CANCELED, RUNNING */
    private String state;
    /** 开始时间戳 */
    private long startTime;
    /** 结束时间戳 */
    private long endTime;
    /** 运行 duration (毫秒) */
    private long duration;
    /** 并行度 */
    private int parallelism;
    /** 最后错误信息 */
    private String lastError;
}
```

### Step 2: 实现 History Server 监控器

**文件：** `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/module/flink/monitor/monitor/HistoryServerMonitor.java`

```java
@Component
@Order(1)
public class HistoryServerMonitor implements FlinkJobMonitor {

    @Value("${flink.history.server.url:}")
    private String historyServerUrl;

    private final RestClient restClient;
    private final Configuration clientConfiguration;

    public HistoryServerMonitor(RestClient restClient, Configuration clientConfiguration) {
        this.restClient = restClient;
        this.clientConfiguration = clientConfiguration;
    }

    @Override
    public JobStatus getJobStatus(DataJobDto job) throws Exception {
        // 1. 构造 History Server URL
        // 2. 调用 REST API 获取作业状态
        // 3. 返回 JobStatus
    }

    /**
     * 获取已完成作业列表
     */
    public List<HistoryJobInfo> getCompletedJobs() {
        // 调用 /jobs 端点获取历史作业
    }

    /**
     * 获取单个历史作业详情
     */
    public Optional<HistoryJobDetailResponse> getJobDetail(String jobId) {
        // 调用 /jobs/{jobId} 端点
    }
}
```

### Step 3: 添加 History Server 配置

**文件：** `yudao-server/src/main/resources/application.yaml`

```yaml
flink:
  history:
    server:
      url: http://history-server-host:8082
      enabled: true
  rest:
    connection-timeout: 2000
    max-content-length: 104857600
```

## 关键文件清单

| 文件 | 操作 | 描述 |
|------|------|------|
| `yudao-flink-common/.../dto/HistoryJobInfo.java` | 新增 | 历史作业信息 DTO |
| `yudao-flink-monitor/.../monitor/HistoryServerMonitor.java` | 新增 | History Server 监控器 |
| `application.yaml` | 修改 | 添加 History Server 配置 |

## 集成说明

集成到现有 monitor 架构：

1. **HistoryServerMonitor 实现 FlinkJobMonitor 接口**
   - 自动被 `JobMonitorFactory` 加载
   - 与其他 Monitor 协同工作

2. **通过 API现有 暴露数据**
   - 复用 `FlinkJobMonitorApi` 接口
   - 不新增独立 Controller

3. **前端集成**
   - 在现有监控页面增加历史作业 tab
   - 调用已有 API 获取数据

## 风险与缓解措施

| 风险 | 缓解措施 |
|------|----------|
| History Server 不可用 | 添加配置开关，优雅降级 |
| REST API 差异 | 使用版本化的 API 接口 |
| 网络超时 | 复用现有 RestClient 配置和超时设置 |

## 依赖检查

**无需新增依赖：**
- `flink-runtime-web` (已有)
- `flink-clients` (已有)
- `RestClient` (已有)

## 下一步

1. 确认 History Server 部署地址
2. 开始 Step 1 实现
